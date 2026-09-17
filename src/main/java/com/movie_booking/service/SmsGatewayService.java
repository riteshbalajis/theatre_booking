/*package com.movie_booking.service;

public class SmsGatewayService {

    private static final String MSGCENTRAL_URL = "https://cpaas.messagecentral.com/verification/v3/send";
    

    public String sendOtp(String countryCode, String mobileNumber, String authToken){

        Client client= ClientBuilder.newClient();

        try {
            // Build target URL with query parameters
            WebTarget target = client.target(BASE_URL)
                    .queryParam("countryCode", countryCode)
                    .queryParam("flowType", "SMS")
                    .queryParam("mobileNumber", mobileNumber);

            Response response = target.request(MediaType.APPLICATION_JSON)
                    .header("authToken", authToken)
                    .post(Entity.entity("", MediaType.APPLICATION_JSON));


            String responseBody = response.readEntity(String.class);
            //if(response.getStatus() ==200){
                return responseBody;
            //}
    
    }


    
}*/
