package googleCloud;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.plaf.synth.SynthSeparatorUI;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BucketGetOption;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.StorageOptions.Builder;


/**
 * Using the Google Cloud Storage API in two ways: SDK and RESTful.
 *
 * @author Dionysis Athanasopoulos
 */
public class GoogleCloudStorage {

	static Builder builder = null;
	static Storage storage = null;

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void setVars (String id, GoogleCredential creds) {

		try {
			String projectID = id;

			builder = StorageOptions.newBuilder();

			// Set the project id
			builder.setProjectId( projectID );

			// Set your credentials
			builder.setCredentials( ServiceAccountCredentials.fromStream( new FileInputStream("credentials/My First Project-53b0b8c14110.json")));

			// Instantiate service client
			storage = builder.build().getService();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void uploadFile (Bucket bucket) {

		BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

		try {
			//Gets user input of file path
			String filePath = input.readLine();
			
			Path path = Paths.get( filePath );
			
			//converts file to byte array for uploading
			byte[] byteArray = java.nio.file.Files.readAllBytes( path );


			System.out.println("What should the file be called?");
			String fileName = input.readLine();

			//uploads the file
			Blob blob = bucket.create(fileName, byteArray, "text/plain");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void createBucket (String id) {

		try{

			System.out.println( "\n---- SDK (START) ----\n" );

			// The name for the new bucket
			System.out.println("What would you like to call your new bucket?");

			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

			//checks if a bucket with that name exists
			String bucketName = doesExist();

			System.out.println( "Creating the bucket " + bucketName + "..." );

			// Creates the new bucket
			Bucket bucket = storage.create( BucketInfo.of( bucketName ) );

			System.out.println( "The bucket " + bucket.getName() + " has been created.\n" );

			System.out.println("Would you like to upload a file? (Y/N)");
			String upload = input.readLine();
			
			if (upload.equals("Y") || upload.equals("y")) {
				System.out.println("Enter the path to the file to upload.");
				uploadFile (bucket);
				System.out.println("File uploaded successfully.");
			}
			

		}

		catch (Exception e) {
			e.printStackTrace();
			//System.out.println( e.getMessage() );
		}

		System.out.println( "\n---- SDK (END) ----\n" );
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void moveContent (String newBucket, String oldBucket) {

		Page<Blob> p = storage.list(oldBucket);

		for (Blob b : p.getValues()){
			b.copyTo(newBucket);
			b.delete();
		}

		System.out.println(oldBucket + " copied successfully.");
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static String checkBucket (String bucket) {

		try {
			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );
			bucket = input.readLine();

			try {
				storage.get(bucket);
			}

			catch (Exception e) {
				System.out.println("No bucket exists with that name.");
				System.out.println("Please try another bucket.");
				String test = checkBucket(bucket);
				bucket = test;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return bucket;
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static String doesExist () {

		BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

		boolean alreadyExists = true;
		String bucketName = "";

		while (alreadyExists) {

			try {
				bucketName = input.readLine();

			} catch (IOException e) {
				e.printStackTrace();
			}

			if (storage.get(bucketName) != null) {
				System.out.println("A bucket with that name already exists.");
				System.out.println("Please choose a new name.");
			}
			else {
				alreadyExists = false;
			}
		}

		return bucketName;
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void mergeBuckets(){

		System.out.println("Please enter the first bucket you wish to merge...");
		String bucket1 = checkBucket("");

		System.out.println("Please enter the second bucket you wish to merge...");
		String bucket2 = checkBucket("");

		System.out.println("Please enter the name of the bucket you wish to merge into (it will be created if it doesn't exist)...");
		String newBucketStr = checkBucket("");

		if (storage.get(newBucketStr) == null){
			Bucket newBucket = storage.create( BucketInfo.of( newBucketStr ) );
			System.out.println(newBucketStr + " created.");
		}

		moveContent(newBucketStr, bucket1);
		moveContent(newBucketStr, bucket2);

		storage.delete(bucket1);
		System.out.println(bucket1 + " deleted.");
		storage.delete(bucket2);
		System.out.println(bucket2 + " deleted.");
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void splitBucket () {

		BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

		Page<Bucket> page = storage.list();
		
		//Makes a map of the buckets to integers
		HashMap<Integer, Bucket> buckets = new HashMap<Integer, Bucket>();
		int counter = 0;

		System.out.println("Please enter which bucket you wish to split.");

		for (Bucket b : page.getValues()) {
			counter++;
			System.out.println(counter + ": " + b.getName());
			buckets.put(counter, b);
		}

		String choice = "";
		try {
			choice = input.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		int choiceInt = Integer.parseInt(choice);
		String toSplit = "";

		for (int i = 0; i < buckets.size(); i++) {
			if (choiceInt == i+1) {
				toSplit = buckets.get(i+1).getName();
			}
		}

		System.out.println(toSplit + " was selected.");

		String bucket1 = "";
		String bucket2 = "";

		bucket1 = doesExist();

		// Creates the new bucket
		Bucket firstbucket = storage.create( BucketInfo.of( bucket1 ) );

		System.out.println( "The bucket " + firstbucket.getName() + " has been created.\n" );

		bucket2 = doesExist();
		// Creates the new bucket
		Bucket secondbucket = storage.create( BucketInfo.of( bucket2 ) );

		System.out.println( "The bucket " + secondbucket.getName() + " has been created.\n" );

		Page<Blob> p = storage.list(toSplit);


		for (Blob b : p.getValues()) {
			System.out.println(b.getName());
			System.out.println("Which bucket would you like to put this into?");
			System.out.println("1: " + bucket1);
			System.out.println("2: " + bucket2);

			choice = "";

			try {
				choice = input.readLine();

				if (choice.equals("1") || choice.equals(bucket1) ) {
					b.copyTo(bucket1);
					b.delete();
				}
				else if (choice.equals("2") || choice.equals(bucket2)) {
					b.copyTo(bucket2);
					b.delete();
				}
				else {
					System.out.println("Invalid input. Please select 1 or 2.");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}				
		}

		System.out.println(toSplit + " split successfully.");

	}

	//---------------------------------------------------------------------------------------------------------------------------------
}
