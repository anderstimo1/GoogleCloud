package googleCloud;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class Main {

	public static void main(String[] args) {

		try {
		GoogleCloudStorage gcs = new GoogleCloudStorage();
		APIMethods api = new APIMethods();
		
		BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );

		System.out.println("Please enter your project-id (e.g. savvy-etching-187922)");
		
		String id = input.readLine();
		
		System.out.println("Please enter the path to your credentials (e.g. credentials/My First Project-53b0b8c14110.json)");
		String creds = input.readLine();
		
		
		GoogleCredential credentials = GoogleCredential.fromStream( new FileInputStream(creds)).createScoped( Collections.singleton( api.getStorageScope() ) );
		

		gcs.setVars(id, credentials);
		api.setProjectID(id);
		api.setCredentials(credentials);

		while(true){
			try{

				System.out.println( "1. SDK" );
				System.out.println( "2. XML API" );
				System.out.println( "3. JSON API" );
				System.out.println( "4. QUT");
				System.out.print( "\n>" );

				String answer = input.readLine();

				int answerInt = 1;
				if (answer == null || (answer != null && answer.equals(""))) answerInt = 1;
				else answerInt = Integer.parseInt( answer );

				if( answerInt == 1 ) {
					System.out.println( "1. Create Bucket" );
					System.out.println( "2. Merge Buckets" );
					System.out.println( "3. Split Bucket" );
					System.out.println( "4. Quit" );

					answer = input.readLine();
					answerInt = 1;
					if (answer == null || (answer != null && answer.equals(""))) answerInt = 1;
					else answerInt = Integer.parseInt( answer );

					if (answerInt == 1) gcs.createBucket(id);
					else if (answerInt == 2) gcs.mergeBuckets();
					else if (answerInt == 3) gcs.splitBucket();
					else if( answerInt == 4 ) {
						System.out.println("Quittin' Time.");
						System.exit(0);
					}

				}
				else if( answerInt == 2 ) {
					System.out.println( "1. Get Bucket" );
					System.out.println( "2. Create Bucket" );
					System.out.println( "3. Merge Buckets" );
					System.out.println( "4. Split Bucket" );
					System.out.println( "5. Upload Object" );
					System.out.println( "6. Print Object" );
					System.out.println( "7. Quit" );

					answer = input.readLine();
					answerInt = 1;
					if (answer == null || (answer != null && answer.equals(""))) answerInt = 1;
					else answerInt = Integer.parseInt( answer );

					if (answerInt == 1) api.xmlAPI_GET();
					else if (answerInt == 2) api.xmlAPI_PUT();
					else if (answerInt == 3) api.mergeBucketXML();
					else if (answerInt == 4) api.splitBucketXML();
					else if (answerInt == 5) {
						System.out.println("Enter the bucket you wish to upload to.");
						api.uploadObject(input.readLine());
					}
					else if( answerInt == 6) api.printObject();
					else if( answerInt == 7 ) {
						System.out.println("Quittin' Time.");
						System.exit(0);
					}

				}
				else if( answerInt == 3 ) api.jsonAPI();
				else if( answerInt == 4 ) {
					System.out.println("Quittin' Time.");
					System.exit(0);
				}
			}

			catch (Exception e) { e.printStackTrace(); }

			System.out.println("");
		}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

}
