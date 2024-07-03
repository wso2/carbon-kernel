package org.wso2.carbon.http.client;

public class HttpClientConstants {

    public enum Error {

        RESPONSE_ENTITY_EMPTY("12001", "Response entity is empty"),
        RESPONSE_PARSE_ERROR("12002", "Error occurred while parsing the response");

        private final String code;
        private final String message;
        private static final String API_ERROR_CODE_PREFIX = "HTC-";

        Error(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return API_ERROR_CODE_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }
    }

}
