package info.wearetrying;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import info.wearetrying.model.PushPayload;
import info.wearetrying.model.PushSubscription;
import nl.martijndwars.webpush.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;

public class Sender {

    private static void sendToNonSuffari(PushSubscription subsObj, PushPayload payloadObj) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        String endpoint = subsObj.getEndpoint();
        PublicKey publicKey = Utils.loadPublicKey(subsObj.getP256dh());
        byte[] auth = Utils.base64Decode(subsObj.getAuth());
        byte[] payload = payloadObj.toString().getBytes();
        // Construct notification
        Notification notification = new Notification(endpoint,publicKey , auth, payload);
        // Construct push service
        PushService pushService = new PushService();
        pushService.setSubject("mailto:adm.trine@gmail.com");
        pushService.setPublicKey(Utils.loadPublicKey(PushServerVars.VAPID_PUBLIC_KEY));
        pushService.setPrivateKey(Utils.loadPrivateKey(PushServerVars.VAPID_PRIVATE_KEY));
        // Send notification!
        HttpResponse httpResponse = pushService.send(notification);

        System.out.println(httpResponse.getStatusLine().getStatusCode());
        System.out.println(IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
    }

    private static void sendToSuffari(String token, String payloadJSON) throws Exception {
        ApnsService service = APNS.newService()
                .withCert("apn_developer_identity.p12", "123456")
                .withAppleDestination(true)
                .build();
        service.push(token, payloadJSON);
    }

    public static void main(String[] args) {
        // Sample payloads
        PushPayload
                nonSufariPayload = new PushPayload("Hi","Test payload");
        String
                safariPayload = "{\"aps\":{\"alert\":{\"title\":\"Flight A998 Now Boarding\",\"body\":\"Boarding has begun for Flight A998.\",\"action\":\"View\"},\"url-args\":[\"https://lahiru.tech/\"]}}";
        // Replace these subscriptions with your valid ones
        PushSubscription
                nonSuffariSubscription = new PushSubscription( "{\"endpoint\":\"https://fcm.googleapis.com/fcm/send/evcR0WFcVY0:APA91bGxifNfKKeOFJBNY28Bdz2D1hbIZbsvBvORyIPErAZBy1i2uQfDBnzNI7x9IxDkWfF6TAqC37NECy9DbV6PPMxvCnlI72qY26KTC7rSmPLe3DUSSjewIynQBFu7i3nuwUEI16_h\",\"keys\":{\"p256dh\":\"BAKzKIzGWjJNnfjuDFey88_qGZw8rufpvKu9jAkraqzvw5IMybTFgllouJFJ0LnjRUwe-vRV3GYDMq0eZjMlI2Y=\",\"auth\":\"OYBGUshZlBFJz4_Q4-YOQA==\"}}");
        String
                suffariSubscription = "D1F415F5FEF4B42879E4AA0AEFD5B0E34D3C1FBA3E73BFE546E4C65E1F552C0B";

        try {
            /*
             * Send push notifications to relevant service endpoints. Messages will be delivered when
             * the subscribed receiver opens browser
             */
            sendToNonSuffari(nonSuffariSubscription, nonSufariPayload);
            sendToSuffari(suffariSubscription, safariPayload);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
