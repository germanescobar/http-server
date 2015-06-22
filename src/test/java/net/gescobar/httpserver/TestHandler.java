
package net.gescobar.httpserver;

class TestHandler implements Handler {

        private Request request;

        @Override
        public final void handle(Request request, Response response) {
                this.request = request;

                doHandle(request, response);
        }

        public void doHandle(Request request, Response response) {

        }

        public Request getRequest() {
                return request;
        }

}
