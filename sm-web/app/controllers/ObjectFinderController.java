package controllers;

import akka.util.ByteString;
import play.mvc.*;
import play.mvc.Security;

public class ObjectFinderController extends Controller {

    //@Security.Authenticated(Secured.class)
    public Result findObject() {
        ByteString byteString = request().body().asBytes();
        //byteString.toB
        return ok("test");
    }
}
