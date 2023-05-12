package com.supriya.doctorApp.services;


import com.supriya.doctorApp.dto.SignInInput;
import com.supriya.doctorApp.dto.SignInOutput;
import com.supriya.doctorApp.dto.SignUpInput;
import com.supriya.doctorApp.dto.SignUpOutput;
import com.supriya.doctorApp.models.AuthenticationToken;
import com.supriya.doctorApp.models.Patient;
import com.supriya.doctorApp.repositories.IPatientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PatientService {

    @Autowired
    IPatientRepo iPatientRepo;


    @Autowired
    AuthenticationService tokenService;




    public SignUpOutput signUp(SignUpInput signUpDto) {

        //check if user exists or not based on email
        Patient patient = iPatientRepo.findFirstByPatientEmail(signUpDto.getUserEmail());

        if(patient != null)
        {
            throw new IllegalStateException("Patient already exists!!!!...sign in instead");
        }


        //encryption
        String encryptedPassword = null;
        try {
            encryptedPassword = encryptPassword(signUpDto.getUserPassword());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }

        //save the user

        patient = new Patient(signUpDto.getUserFirstName(),
                signUpDto.getUserLastName(),signUpDto.getUserEmail(),
                encryptedPassword, signUpDto.getUserContact());

        iPatientRepo.save(patient);

        //token creation and saving

        AuthenticationToken token = new AuthenticationToken(patient);

        tokenService.saveToken(token);

        return new SignUpOutput("Patient registered","Patient created successfully");


    }

    private String encryptPassword(String userPassword) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(userPassword.getBytes());
        byte[] digested =  md5.digest();

        String hash = DatatypeConverter.printHexBinary(digested);

        //For direct convert into string =>
        //  String hash = new String(digested);

        return hash;
    }


    public SignInOutput signIn(SignInInput signInDto) {

        //get email

        Patient patient = iPatientRepo.findFirstByPatientEmail(signInDto.getPatientEmail());

        if(patient == null)
        {
            throw new IllegalStateException("User invalid!!!!...sign up instead");
        }

        //encrypt the password

        String encryptedPassword = null;

        try {
            encryptedPassword = encryptPassword(signInDto.getPatientPassword());
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }



        //match it with database encrypted password

        boolean isPasswordValid = encryptedPassword.equals(patient.getPatientPassword());

        if(!isPasswordValid)
        {
            throw new IllegalStateException("User invalid!!!!...sign up instead");
        }

        //figure out the token

        AuthenticationToken authToken = tokenService.getToken(patient);

        //set up output response

        return new SignInOutput("Authentication Successful..!!!",authToken.getToken());



    }
}
