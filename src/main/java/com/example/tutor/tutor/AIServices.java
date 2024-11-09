package com.example.tutor.tutor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptResponseFormat;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.FileSystemResource;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



@Service
public class AIServices {

    @Autowired
   private OpenAiAudioApi openAiAudioApi;

   
    public ChatClient chatClient;
public AIServices(ChatClient.Builder builder){
this.chatClient=builder.build();
}
    
    
    public String generateTrans(MultipartFile file) throws IOException {

        File tempFile = File.createTempFile("audio", ".mp3");
        file.transferTo(tempFile);
    

   
var openAiAudioTranscriptionModel = new OpenAiAudioTranscriptionModel(openAiAudioApi);

var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
    .withResponseFormat(TranscriptResponseFormat.TEXT)
    .withTemperature(0f)
    .build();

    var audioFile = new FileSystemResource(tempFile);
AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
AudioTranscriptionResponse response =  openAiAudioTranscriptionModel.call(transcriptionRequest);
        tempFile.delete();
        return response.getResult().getOutput();
    }

 public byte[] Speechgen(String text){

    if (text == null || text.trim().isEmpty()) {
        throw new IllegalArgumentException("Text input cannot be null or empty.");
    }

    try {
  
        var openAiAudioSpeechModel = new OpenAiAudioSpeechModel(openAiAudioApi);

        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder()
            .withModel("tts-1")
            .withVoice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
            .withResponseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
            .withSpeed(1.0f)
            .build();

        SpeechPrompt speechPrompt = new SpeechPrompt(text, speechOptions);

       
        SpeechResponse response = openAiAudioSpeechModel.call(speechPrompt);

        // Get the audio output as a byte array
        byte[] audioData = response.getResult().getOutput();

        if (audioData == null || audioData.length == 0) {
            throw new RuntimeException("No audio data was generated.");
        }

        return audioData;

    } catch (IllegalArgumentException e) {
       
        throw e;

    }  catch (Exception e) {
      
        throw new RuntimeException("An error occurred during speech generation: " + e.getMessage(), e);
    }
 }
      
public String GPTChat(SystemMessage systemmess,UserMessage usermess){


    try {
        // Generate feedback by calling the GPT client
        String feedback = this.chatClient.prompt()
                .system(systemmess.getContent())
                .user(usermess.getContent())
                .call()
                .content();

        return feedback;
                
    } catch (Exception e) {
       
        return "Unable to understand.";
    }


}
   
}
