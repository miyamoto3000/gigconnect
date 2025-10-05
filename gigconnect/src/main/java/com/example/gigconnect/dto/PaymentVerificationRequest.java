// Create new file: src/main/java/com/example/gigconnect/dto/PaymentVerificationRequest.java
package com.example.gigconnect.dto;

import lombok.Data;
import org.json.JSONObject;

@Data
public class PaymentVerificationRequest {
    private String razorpay_payment_id;
    private String razorpay_order_id;
    private String razorpay_signature;

    public JSONObject getProperties() {
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", this.razorpay_order_id);
        options.put("razorpay_payment_id", this.razorpay_payment_id);
        options.put("razorpay_signature", this.razorpay_signature);
        return options;
    }
}