package com.example.booprachat.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:Key=AAAAlT6E8BQ:APA91bHO1CWzvho2Irg2tAvnn4DxlsMH6wTQ7E2GOfqxDNkO1ZwdnkUEmWOshUug731cQSeTwhp64LSG4pHQxLZd8BnUXBoA4NLtGwNQGDuPj1pjVor4H8pCGwJYvBiRt8CsPJvtz5AL"
            }
    )
    @POST("fcm/send")
    Call<Response>sendNotification(@Body Sender body);
}
