package googleCloud;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin.Response;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.cloud.storage.Bucket;

public class APIMethods {

	private static final String STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.full_control";

	private static String projectID = "";
	static GoogleCredential credentials = null;

	public void setProjectID (String id) {
		projectID = id;
	}

	public void setCredentials (GoogleCredential creds) {
		credentials = creds;
	}

	public String getStorageScope() {
		return STORAGE_SCOPE;
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void xmlAPI_GET() {

		try{

			System.out.println( "---- xmlAPI ----\n" );


			// Set your credentials
			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream("credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			String bucketName = "";

			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

			System.out.println("Please enter the name of the bucket you want to view.");
			bucketName = input.readLine();

			// Set up the endpoint - URI of the Google Cloud Storage RESTful (get the content of a bucket).
			String uri = "https://storage.googleapis.com/" + URLEncoder.encode( bucketName, "UTF-8" );

			// Include your credentials in the Authorization header of the HTTP request
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );
			GenericUrl url = new GenericUrl( uri );

			HttpRequest request = requestFactory.buildGetRequest( url );

			// Execute the HTTP request
			HttpResponse response = request.execute();
			String content = response.parseAsString();

			//System.out.println( content );

			prettyPrintXml( bucketName, content );


			System.out.println( "---- xmlAPI ----\n" );
		}

		catch (Exception e) { e.printStackTrace(); }
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static String xmlAPI_PUT() {

		GoogleCloudStorage gcs = new GoogleCloudStorage();
		String bucketName = "";

		try{

			System.out.println( "\n\n---- xmlAPI ----\n" );


			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream( "credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			//			String projectID = "";
			//
			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );
			//
			//			System.out.println("Please enter the ID of your project.");
			//			projectID = input.readLine();

			System.out.println("Please enter a name for the new bucket.");
			bucketName = gcs.doesExist();

			String uri = "https://storage.googleapis.com/" + URLEncoder.encode( bucketName, "UTF-8" );
			GenericUrl url = new GenericUrl( uri );

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );

			StringBuffer data = new StringBuffer();
			data.append( "<CreateBucketConfiguration>" ).append("\n");
			data.append( "<LocationConstraint>australia-southeast1</LocationConstraint>" ).append("\n");
			data.append( "<StorageClass>REGIONAL</StorageClass>" ).append("\n");

			HttpContent httpContent = new UrlEncodedContent( data );
			HttpRequest request = requestFactory.buildPutRequest( url, httpContent );
			request.getHeaders().set( "x-goog-project-id", projectID );

			HttpResponse response = request.execute();
			response.parseAsString();

			System.out.println( "The bucket " + bucketName + " has been created.\n" );

			System.out.println("Do you want to upload a file? (Y/N)");
			String upload = input.readLine();

			if (upload.equals("Y") || upload.equals("y")){
				uploadObject(bucketName);
			}

			System.out.println( "---- xmlAPI ----\n" );
		}

		catch (Exception e) { e.printStackTrace(); }

		return bucketName;
	}

	//---------------------------------------------------------------------------------------------------------------------------------	

	public static void mergeBucketXML () {
		try{

			System.out.println( "---- xmlAPI ----\n" );

			// Set your credentials
			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream("credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			//Get bucket name from the user
			String sourceBucket1 = "";
			String sourceBucket2 = "";

			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

			System.out.println("Please enter the first source bucket.");
			sourceBucket1 = input.readLine();

			System.out.println("Please enter the second source bucket.");
			sourceBucket2 = input.readLine();

			// Set up the endpoint - URI of the Google Cloud Storage RESTful (get the content of a bucket).
			String uri = "https://storage.googleapis.com/" + URLEncoder.encode( sourceBucket1, "UTF-8" );

			// Include your credentials in the Authorization header of the HTTP request
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );
			GenericUrl url = new GenericUrl( uri );

			HttpRequest request = requestFactory.buildGetRequest( url );

			// Execute the HTTP request
			HttpResponse response = request.execute();

			//reads the XML from the response
			String xml = response.parseAsString();

			//Converts the string to a navigable XML document
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			Element rootElement = document.getDocumentElement();

			//Finds all elements with the tag "key"
			NodeList nodeList = rootElement.getElementsByTagName("Key");

			String itemName = "";
			String targetBucket = "";

			//Get the target bucket from user input.
			System.out.println("Would you like to make a new bucket? (Y/N)");
			String yesNo = input.readLine();
			if (yesNo.equals("Y")) {
				targetBucket = xmlAPI_PUT();
			}
			else {
				System.out.println("Enter the name of your target bucket.");
				targetBucket = input.readLine();				
			}

			//loops over all of the tags found above
			for (int i = 0; i < nodeList.getLength(); i++) {
				itemName = nodeList.item(i).getTextContent();

				copyItem(sourceBucket1, targetBucket, itemName);
			}

			System.out.println(sourceBucket1 + " copied.");
			deleteEmptyBucket(sourceBucket1);
			System.out.println(sourceBucket1 + " deleted.");

			uri = "https://storage.googleapis.com/" + URLEncoder.encode( sourceBucket2, "UTF-8" );

			// Include your credentials in the Authorization header of the HTTP request
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			requestFactory = httpTransport.createRequestFactory( credentials );
			url = new GenericUrl( uri );

			request = requestFactory.buildGetRequest( url );

			// Execute the HTTP request
			response = request.execute();

			//reads the XML from the response
			xml = response.parseAsString();

			//Converts the string to a navigable XML document
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(new StringReader(xml)));
			rootElement = document.getDocumentElement();

			//Finds all elements with the tag "key"
			nodeList = rootElement.getElementsByTagName("Key");

			itemName = "";

			//loops over all of the tags found above
			for (int i = 0; i < nodeList.getLength(); i++) {
				itemName = nodeList.item(i).getTextContent();

				copyItem(sourceBucket2, targetBucket, itemName);
			}

			System.out.println(sourceBucket2 + " copied.");

			deleteEmptyBucket(sourceBucket2);
			System.out.println(sourceBucket2 + " deleted.");

			System.out.println("Merge complete.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void copyItem (String sourceBucket, String targetBucket, String itemName) {

		try {

			// Set your credentials
			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream("credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );

			//Gets user input of the bucket to put into
			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

			//sets the URL
			String uri = "http://storage.googleapis.com/" + targetBucket + "/" + itemName;
			//			System.out.println( uri );
			GenericUrl url = new GenericUrl( uri );

			// Include your credentials in the Authorization header of the HTTP request
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			requestFactory = httpTransport.createRequestFactory( credentials );

			//Adds body to the request
			StringBuffer data = new StringBuffer();
			data.append( "<CreateBucketConfiguration>" ).append("\n");
			data.append( "<LocationConstraint>australia-southeast1</LocationConstraint>" ).append("\n");
			data.append( "<StorageClass>REGIONAL</StorageClass>" ).append("\n");

			//Builds a put request
			HttpContent httpContent = new UrlEncodedContent( data );
			HttpRequest putRequest = requestFactory.buildPutRequest(url, httpContent);

			//Adds copy source to header (tells API to copy item from URL instead of upload a new one)
			putRequest.getHeaders().set( "x-goog-project-id", projectID );
			putRequest.getHeaders().setContentType( "text/plain" );
			putRequest.getHeaders().set( "x-goog-copy-source",  sourceBucket + "/" + itemName );

			//executes the request
			HttpResponse response = putRequest.execute();

			System.out.println(itemName + " copied successfully.");

			deleteObject(sourceBucket, itemName);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void deleteObject(String bucketName, String objectName) {

		try{

			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream( "credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			String uri = "https://" + bucketName + ".storage.googleapis.com/" + URLEncoder.encode( objectName, "UTF-8" );
			GenericUrl url = new GenericUrl( uri );

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );

			HttpRequest request = requestFactory.buildDeleteRequest( url );
			request.getHeaders().set( "x-goog-project-id", projectID );

			request.execute();


			System.out.println("\nObject deleted\n" );
		}

		catch (Exception e) { e.printStackTrace(); }
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void deleteEmptyBucket(String bucketName) {

		try{

			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream( "credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			String uri = "https://" + bucketName + ".storage.googleapis.com";
			GenericUrl url = new GenericUrl( uri );

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );

			HttpRequest request = requestFactory.buildDeleteRequest( url );
			request.getHeaders().set( "x-goog-project-id", projectID );

			request.execute();


			System.out.println("\nBucket deleted\n" );
		}

		catch (Exception e) { e.printStackTrace(); }
	}


	//---------------------------------------------------------------------------------------------------------------------------------

	public static void splitBucketXML () {

		try {
			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

			String sourceBucket = "";
			String targetBucket1 = "";
			String targetBucket2 = "";

			try {
				System.out.println("Please enter the name of the bucket you wish to split.");
				sourceBucket = input.readLine();

			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println("Please enter the name of the first target bucket.");
			targetBucket1 = xmlAPI_PUT();

			System.out.println("Please enter the name of the second target bucket.");
			targetBucket2 = xmlAPI_PUT();

			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream("credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			// Set up the endpoint - URI of the Google Cloud Storage RESTful (get the content of a bucket).
			String uri = "https://storage.googleapis.com/" + URLEncoder.encode( sourceBucket , "UTF-8" );

			// Include your credentials in the Authorization header of the HTTP request
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );
			GenericUrl url = new GenericUrl( uri );

			HttpRequest request = requestFactory.buildGetRequest( url );

			// Execute the HTTP request
			HttpResponse response = request.execute();

			//reads the XML from the response
			String xml = response.parseAsString();

			//Converts the string to a navigable XML document
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xml)));
			Element rootElement = document.getDocumentElement();

			//Finds all elements with the tag "key"
			NodeList nodeList = rootElement.getElementsByTagName("Key");

			String itemName = "";

			//loops over all of the tags found above
			for (int i = 0; i < nodeList.getLength(); i++) {
				itemName = nodeList.item(i).getTextContent();

				System.out.println(nodeList.item(i).getTextContent());
				System.out.println("Which bucket do you want to put " + nodeList.item(i).getTextContent() + " in to?");
				System.out.println("1: " + targetBucket1);
				System.out.println("2: " + targetBucket2);

				String destination = input.readLine();
				if (destination.equals("1")) {
					copyItem(sourceBucket, targetBucket1, itemName);
					System.out.println(itemName + " copied.");
				}
				else {
					copyItem(sourceBucket, targetBucket2, itemName);
					System.out.println(itemName + " copied.");
				}
			}

			for (int i = 0; i < nodeList.getLength(); i++) {
				itemName = nodeList.item(i).getTextContent();

				deleteObject(sourceBucket, itemName);
			}

			deleteEmptyBucket(sourceBucket);
			System.out.println(sourceBucket + " deleted.");
			System.out.println("Split complete.");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void uploadObject (String bucket) {

		try{

			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream( "credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

			String objectName = "toUpload.txt";

			String uri = "https://" + bucket + ".storage.googleapis.com/" + URLEncoder.encode( objectName, "UTF-8" );
			GenericUrl url = new GenericUrl( uri );

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );

			System.out.println("Please enter the path to the file you want to upload");
			String filePath = input.readLine();

			Path path = Paths.get( filePath );
			byte[] byteArray = java.nio.file.Files.readAllBytes( path );
			HttpContent httpContent = new ByteArrayContent( "text/plain", byteArray );

			HttpRequest request = requestFactory.buildPutRequest( url, httpContent );
			request.getHeaders().set( "x-goog-metadata-directive", "REPLACE" );
			request.getHeaders().set( "x-goog-project-id", projectID );

			request.execute();


			System.out.println("\nObject uploaded\n" );
		}

		catch (Exception e) { e.printStackTrace(); }
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void printObject () {

		try { 

			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );

			//Gets user input of the bucket to put into
			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

			String bucketName = "";
			String itemName = "";

			try {

				System.out.println("Enter the name of the bucket you wish to view an item in.");
				bucketName = input.readLine();

				System.out.println("Enter the name of the file you wish to view.");
				itemName = input.readLine();

			} catch (IOException e) {
				e.printStackTrace();
			}

			//sets the URL
			String uri = "http://storage.googleapis.com/" + bucketName + "/" + itemName;
			GenericUrl url = new GenericUrl( uri );

			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			requestFactory = httpTransport.createRequestFactory( credentials );

			//Builds a put request
			HttpRequest request = requestFactory.buildGetRequest(url);

			request.getHeaders().set( "x-goog-project-id", projectID );
			request.getHeaders().setContentType( "text/plain" );
			
			HttpResponse response = request.execute();
			
			//reads the XML from the response
			String xml = response.parseAsString();
			
			System.out.println("");
			System.out.println("/----------- File Contents Start ------------/");
			System.out.println("");
			System.out.println(xml);
			System.out.println("");
			System.out.println("/------------ File Contents End -------------/");			
			System.out.println("");


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	private static void prettyPrintXml( final String bucketName, final String content) {

		// Instantiate transformer input.
		Source xmlInput = new StreamSource(new StringReader(content));
		StreamResult xmlOutput = new StreamResult(new StringWriter());

		// Configure transformer.
		try {

			Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An identity transformer
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "testing.dtd");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(xmlInput, xmlOutput);

			// Pretty print the output XML.
			System.out.println("\nBucket listing for " + bucketName + ":\n");
			System.out.println(xmlOutput.getWriter().toString());
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void jsonAPI() {

		try{

			System.out.println( "---- jsonAPI ----\n" );


			// Set your credentials

			String bucketName = "victoria-tim-bucket-json";

			// Set up the endpoint - URI of the Google Cloud Storage RESTful.
			String uri = "https://www.googleapis.com/storage/v1/b/" + URLEncoder.encode( bucketName, "UTF-8" ) + "/o";

			// Include your credentials in the Authorization header of the HTTP request
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );
			GenericUrl url = new GenericUrl( uri );

			HttpRequest request = requestFactory.buildGetRequest( url );

			// Execute the HTTP request
			HttpResponse response = request.execute();
			String content = response.parseAsString();

			System.out.println( content );


			System.out.println( "---- jsonAPI ----\n" );
		}

		catch (Exception e) { e.printStackTrace(); }
	}

	//---------------------------------------------------------------------------------------------------------------------------------

	public static void createBucketJSON () {

		try{

			System.out.println( "---- jsonAPI ----\n" );


			// Set your credentials
			GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream("credentials/My First Project-53b0b8c14110.json")).createScoped( Collections.singleton( STORAGE_SCOPE ) );

			String bucketName = "victoria-tim-bucket-json";

			// Set up the endpoint - URI of the Google Cloud Storage RESTful.
			String uri = "https://www.googleapis.com/storage/v1/b?project=savvy-etching-187922";

			// Include your credentials in the Authorization header of the HTTP request
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			HttpRequestFactory requestFactory = httpTransport.createRequestFactory( credentials );
			GenericUrl url = new GenericUrl( uri );

			StringBuffer data = new StringBuffer();
			data.append("{").append("\n");
			data.append("\"name\": \"" + bucketName + "\"").append("\n");
			data.append("}").append("\n");

			HttpContent httpContent = new UrlEncodedContent( data );

			HttpRequest request = requestFactory.buildPostRequest( url, httpContent );

			// Execute the HTTP request
			HttpResponse response = request.execute();
			String content = response.parseAsString();

			System.out.println( content );


			System.out.println( "---- jsonAPI ----\n" );
		}

		catch (Exception e) { e.printStackTrace(); }
	}
}
