package org.example;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Main {
    private static final String PRIVATE_KEY_PATH = "src/main/resources/travel-agency-26310-firebase-adminsdk-zd103-2271e6dfbf.json";

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        var serviceAccount = new FileInputStream(getAbsolutePath(PRIVATE_KEY_PATH));
        var options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        Firestore db = FirestoreClient.getFirestore();

        /* GET PROGRAM WITH ID = 1 */
        System.out.println("GET PROGRAM WITH ID = 1");
        var firstProgram = db.collection("program").document("1").get().get();
        if (firstProgram.exists()) System.out.println(firstProgram.getString("title"));
        System.out.println();

        /* GET PROGRAMS WITH MORE THAN 7 DAYS */
        System.out.println("GET PROGRAMS WITH MORE THAN 7 DAYS");
        var programsWithMoreThan7Days = db.collection("program").whereGreaterThan("days_nr", 7).get().get().getDocuments();
        programsWithMoreThan7Days
                .forEach(result -> System.out.println(result.getString("title") + ", days_nr: " + result.getLong("days_nr")));
        System.out.println();

        /* GET ENROLLMENTS FOR ATTRACTIONS FOR BOOKING WITH ID = 1 */
        System.out.println("GET ENROLLMENTS FOR ATTRACTIONS FOR BOOKING WITH ID = 1");
        var firstBookingEnrollments = db.collection("bookings").document("1").collection("enrollments").get().get().getDocuments();
        firstBookingEnrollments
                .forEach(enrollment -> {
                    var attractionId = enrollment.getLong("attraction_id");
                    var programId = enrollment.getLong("program_id");
                    String attractionTitle;
                    try {
                        attractionTitle = db.collection("program")
                                .document(String.valueOf(programId))
                                .collection("attractions")
                                .document(String.valueOf(attractionId)).get().get().getString("title");
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(attractionTitle);
                });
    }

    private static String getAbsolutePath(String resource) {
        return new File(resource).getAbsolutePath();
    }
}