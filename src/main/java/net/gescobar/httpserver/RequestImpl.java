package net.gescobar.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * This is an internal implementation of the {@link Request} interface.
 * 
 * @author German Escobar
 */
class RequestImpl implements Request {

        /**
         * The Host header.
         */
        private String host;

        /**
         * The HTTP method
         */
        private final String method;

        /**
         * The request path
         */
        private final String path;

        /**
         * The request headers
         */
        private final Map<String,String> headers;

        /**
         * Constructor.
         * 
         * @param reader from which we are reading the headers. 
         * 
         * @throws IOException if an I/O error occurs in the underlying connection.
         */
        public RequestImpl(BufferedReader reader) throws IOException {

                String request = reader.readLine();

                // get the method and the path
                method = request.split(" ")[0];
                path = request.split(" ")[1];

                // get the headers
                headers = retrieveHeaders(reader);

        }

        /**
         * Helper method. Retrieves the headers of the request.
         * 
         * @param reader the reader from which we are retrieving the request information.
         * 
         * @return a Map<String,String> object with the headers of the request.
         * @throws IOException if an I/O error occurs in the underlying communication.
         */
        private Map<String,String> retrieveHeaders(BufferedReader reader) throws IOException {

                Map<String,String> headers = new HashMap<String,String>();

                // iterate through the headers
                String headerLine = reader.readLine();
                while( !headerLine.equals("") ) {

                        // headers come in the form "name: value"
                        String name = headerLine.split(":")[0].trim();
                        String value = headerLine.split(":")[1].trim();

                        // add to the headers only if there is no corresponding field (e.g. "Host" header is mapped to the 
                        // *host* field of the request)
                        if ( !isKnownHeader(name, value) ) {
                                headers.put(name, value);
                        }

                        // read next line
                        headerLine = reader.readLine();
                }

                return headers;

        }

        /**
         * Checks if it is a known header and sets the corresponding field.
         * 
         * @param name the name of the header to check.
         * @param value the value of the header to check.
         * 
         * @return true if it is a known header, false otherwise
         */
        private boolean isKnownHeader(String name, String value) {

                boolean ret = false;

                if (name.equalsIgnoreCase("host")) {
                        host = value;
                        return true;
                }

                return ret;

        }

        @Override
        public String getMethod() {
                return method;
        }

        @Override
        public String getPath() {
                return path;
        }

        @Override
        public String getHost() {
                return host;
        }

        @Override
        public Map<String, String> getHeaders() {
                return headers;
        }

        @Override
        public String getHeader(String name) {
                return headers.get(name);
        }

}