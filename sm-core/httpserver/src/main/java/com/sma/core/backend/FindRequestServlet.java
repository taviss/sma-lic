package com.sma.core.backend;

import com.sma.core.camera.st.impl.ImageCamera;
import com.sma.core.object.finder.service.ObjectFinderService;
import com.sma.object.finder.tf.TensorflowImageClassifier;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@WebServlet(name = "FindRequestServlet", urlPatterns = {"/find"}, loadOnStartup = 1)
public class FindRequestServlet extends HttpServlet {
    private ObjectFinderService objectFinderService = new ObjectFinderService();

    public FindRequestServlet() {
        //FIXME Demo
        this.objectFinderService.addCamera(new ImageCamera("test", new File("D:\\Facultate\\LIC_SMA\\sm-core\\object-recognizer\\src\\main\\resources\\puppy_224.jpg")));
        this.objectFinderService.bindObjectRecoginzer(new TensorflowImageClassifier("D:\\Facultate\\LIC_SMA\\sm-core\\object-recognizer\\src\\main\\resources"));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Request received!");

        InputStream inputStream = req.getInputStream();
        byte[] img = new byte[inputStream.available()];
        inputStream.read(img, 0, inputStream.available());

        objectFinderService.findObject(img);
    }
}
