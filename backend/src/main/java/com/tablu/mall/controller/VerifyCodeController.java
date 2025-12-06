package com.tablu.mall.controller;

import com.tablu.mall.utils.VerificationCodeUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * PUBLIC_INTERFACE
 * VerifyCodeController
 *
 * This controller exposes a simple verification code (captcha-like) endpoint used by the frontend
 * login and payment flows. It generates an image and stores the verification text into the HTTP session
 * under attribute name "verify_code". The image is returned as JPEG content.
 *
 * Route:
 *  - GET /verifyCode
 *
 * Parameters:
 *  - none; a cache-busting query param like ?time=... may be sent by clients and is ignored.
 *
 * Returns:
 *  - image/jpeg binary with the verification code image.
 */
@RestController
public class VerifyCodeController {

    /**
     * PUBLIC_INTERFACE
     * Generates a verification image and stores the generated text into the session.
     *
     * @param request  HttpServletRequest to access/create the session
     * @param response HttpServletResponse used to write the image
     * @throws IOException if an error occurs while writing the image
     */
    @GetMapping(value = "/verifyCode", produces = MediaType.IMAGE_JPEG_VALUE)
    public void getVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        VerificationCodeUtil util = new VerificationCodeUtil();
        BufferedImage image = util.getImage();
        // Save code to session for later validation in LoginFilter
        request.getSession().setAttribute("verify_code", util.getText());

        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        // Prevent caching
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        try (OutputStream os = response.getOutputStream()) {
            ImageIO.write(image, "JPEG", os);
            os.flush();
        }
    }
}
