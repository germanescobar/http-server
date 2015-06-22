
package net.gescobar.httpserver;

/**
 * This is a private implementation of the {@link Response interface}
 * 
 * @author German Escobar
 */
class ResponseImpl implements Response {

        private Response.HttpStatus status = Response.HttpStatus.OK;

        private String contentType;

        @Override
        public Response status(Response.HttpStatus status) {
                this.status = status;

                return this;
        }

        @Override
        public Response ok() {
                return status(Response.HttpStatus.OK);
        }

        @Override
        public Response notFound() {
                return status(Response.HttpStatus.NOT_FOUND);
        }

        @Override
        public Response contentType(String contentType) {
                this.contentType = contentType;

                return this;
        }

        public String toString() {
                String ret = "HTTP/1.1 " + status.getCode() + " " + status.getReason() + "\r\n";

                if (contentType != null) {
                        ret += "Content-Type: " + contentType + "\r\n";
                }

                return ret + "\r\n";
        }

}
