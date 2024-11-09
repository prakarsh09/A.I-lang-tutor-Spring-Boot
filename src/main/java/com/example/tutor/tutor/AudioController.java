package com.example.tutor.tutor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;



@RestController
@CrossOrigin(origins = "*")
public class AudioController {

    private final Set<String> recentSentences = new HashSet<>();
    private static final int MAX_RECENT_SENTENCES = 20;

    

    

    @Autowired
    private AIServices aiServices;

    @PostMapping("/process")
    public ResponseEntity<String> processInput(@RequestParam("file") MultipartFile file)throws IOException {
      
       String transres;
    try {
        transres = aiServices.generateTrans(file);
        System.out.println(transres);

        return ResponseEntity.ok(transres);
    } catch (IOException e) {
       
        e.printStackTrace();
        return ResponseEntity.ok("error");
    }
        
        
       
    }

    @GetMapping("/ask")
    public String askGpt(@RequestParam("gen") String gen, @RequestParam("trans") String res, @RequestParam("targetLanguage") String tar, @RequestParam("language") String natlang){
        String gentrans=translate(natlang,tar,gen);
        SystemMessage systemMessage = new SystemMessage("You are a "+tar+" teacher. Help the student by checking if the student's response matches the teacher's sentence. If not, suggest corrections and topics to study , explain in student's native language "+natlang+".");
        
       
        UserMessage userMessage = new UserMessage("Teacher's sentence: " + gentrans + ". Student's response: " + res);
        
        try {
           
            
    
            return aiServices.GPTChat(systemMessage, userMessage);
                    
        } catch (Exception e) {
          
            return "Unable to provide feedback at the moment. Please try again.";
        }
    }


    @GetMapping("/generate")
    public String generateGPT(@RequestParam String language) {

        SystemMessage systemMessage = new SystemMessage("You are a creative "+language+" sentence generator.");
        UserMessage userMessage = new UserMessage(
            "Generate a unique, random sentence in "+language+". Avoid repeating recently generated sentences, " +
            "and consider using varied topics or contexts each time. Also avoid large sentences ");

        String feedback;
        int attempts = 0;
        
        try {
            do {
                feedback = aiServices.GPTChat(systemMessage, userMessage);
                
                attempts++;
            } while (recentSentences.contains(feedback) && attempts < 5); // Avoid repeating up to 5 tries

            if (recentSentences.size() >= MAX_RECENT_SENTENCES) {
                recentSentences.remove(recentSentences.iterator().next()); // Remove the oldest entry if limit reached
            }

            recentSentences.add(feedback);
            return feedback;

        } catch (Exception e) {
            return "Unable to generate. Please try again.";
        }
    }

   
    public String translate( String natlang,String targetlang,String gentext){
      
         SystemMessage systemMessage = new SystemMessage("You are an expert "+natlang+" to "+targetlang+" translator");
        

         UserMessage userMessage = new UserMessage("translate this "+gentext+"in "+targetlang+" , the response should only contain translation");
         
         try {
            
     
             return aiServices.GPTChat(systemMessage, userMessage);
                     
         } catch (Exception e) {
            
             return "Unable to provide translation. Please try again.";
         }


    }

    @GetMapping("/genspeech")
    public ResponseEntity<byte[]> GenSpeech(@RequestParam String text) {
        try {
          
            byte[] audioData = aiServices.Speechgen(text);
    
            // Set headers for an MP3 file response
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "speech.mp3");
    
            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
    
        } catch (IllegalArgumentException e) {
           
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(("Invalid input: " + e.getMessage()).getBytes());
                    
        } catch (Exception e) {
            
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("An error occurred: " + e.getMessage()).getBytes());
        }

        

    }
    
   
    
}