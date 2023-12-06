# perceptor-client-lib-java


## Usage

If you use a locally stored package, add a file source, for example:

```gradle
repositories {
    mavenCentral()
    flatDir {
    dirs '../packages'
    }
}
```

Add following dependencies to your java project:

```gradle
implementation ':perceptor_client_lib:0.1-dev01'
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20")
```

To access the client, following package must be imported:
```java
import org.tamedai.perceptorclient.*;
```


To create a client instance
```java
        var waitTimeout = java.time.Duration.ofSeconds(30);
        ClientSettings settings = new ClientSettings("API_KEY",
                "https://perceptor-api.tamed.ai/1/model/",
                waitTimeout);
        PerceptorClient client = PerceptorClientFactory.INSTANCE.createClient(settings);
```

It is also possible to read the main client settings (_api_key_ and _api_url_) from environment variables. 
Following environment variables will be used:
_TAI_PERCEPTOR_BASE_URL_ for api url
_TAI_PERCEPTOR_API_KEY_ for api key

```java
ClientSettings settings = ClientSettings.Factory.fromEnv();
```

Should those variables be missing or empty, an exception will be thrown.

    
### Creating request
Use method _PerceptorRequest.Factory.withFlavor_ to create a request object without additional parameters.
You have to specify the flavor name and binary flag whether the scores are to be calculated or not.
To specify additional parameters use the constructor of _PerceptorRequest_.

Example code to create a client instance and send a instruction for a text:

```java
import org.tamedai.perceptorclient.*;
import java.util.Arrays;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        var waitTimeout = java.time.Duration.ofSeconds(30);
        ClientSettings settings = new ClientSettings("API_KEY",
                "https://perceptor-api.tamed.ai/1/model/",
                waitTimeout);
        PerceptorClient client = PerceptorClientFactory.INSTANCE.createClient(settings);

        String textToProcess ="Ich melde einen Schaden für meinen Kunden Hans Helmut. Meine Vermittlernumer ist die 090.100.";

        List<String> instructions = Arrays.asList("Vorname und Nachname des Kunden?",
                "Vermittlernumer?");

        var res = client.askText(textToProcess,
                PerceptorRequest.Factory.withFlavor("original", true),
                instructions
        );

        InstructionWithResult firstResp = res.get(0);

        if (firstResp.isSuccess()){
            System.out.println("Got response:");
            System.out.println(firstResp.getResponse().get("text"));
        }else{
            System.out.println("Got error:");
            System.out.println(firstResp.getErrorText());
        }

    }
}
```

Example code to create a client to send an instruction for an image:

```java
import org.tamedai.perceptorclient.*;
import java.util.Arrays;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        var waitTimeout = java.time.Duration.ofSeconds(30);
        ClientSettings settings = new ClientSettings("API_KEY",
                "https://perceptor-api.tamed.ai/1/model/",
                waitTimeout);
        PerceptorClient client = PerceptorClientFactory.INSTANCE.createClient(settings);

        var responseImage = client.askImage("path_to_image",
                PerceptorRequest.Factory.withFlavor("original"),
                Arrays.asList("What is the invoice number?",
                        "What is the invoice date?"));

        InstructionWithResult firstResp = res.get(0);

        if (firstResp.isSuccess()){
            System.out.println("Got response:");
            System.out.println(firstResp.getResponse().get("text"));
        }else{
            System.out.println("Got error:");
            System.out.println(firstResp.getErrorText());
        }

    }
}
```

Example code to create a client to send a classify instruction for an image:

```java
import org.tamedai.perceptorclient.*;
import java.util.Arrays;
import java.util.List;


public class Main {
    public static void main(String[] args) {

        var waitTimeout = java.time.Duration.ofSeconds(30);
        ClientSettings settings = new ClientSettings("API_KEY",
                "https://perceptor-api.tamed.ai/1/model/",
                waitTimeout);
        PerceptorClient client = PerceptorClientFactory.INSTANCE.createClient(settings);

        var response = client.classifyImage(_invoiceImagePath,
                PerceptorRequest.Factory.withFlavor("original", true),
                "what kind of document is it?",
                Arrays.asList("invoice", "application", "prescription")
        );

        if (response.isSuccess()) {
            System.out.printf("Instruction: %s, response: %s", response.getInstruction(), response.getResponse());

        } else {
            System.out.printf("Instruction: %s, error: %s", response.getInstruction(), response.getErrorText());
        }

    }
}
```

### Reading responses

Basic class containing the processing result is _InstructionWithResult_ ([see here](/src/main/kotlin/org/tamedai/perceptorclient/ExternalModels.kt)).

It contains following properties:<br>
_instruction_ contains the original instruction text<br>
_isSuccess_  set to True if the query was successful<br>
_response_ is a map/dictionary containing at least "text" element (with actual response text) and may contain additional values (for example scores).<br>
_errorText_ error text (if error occurred)<br>

Following methods return the list of _InstructionWithResult_ instances:<br>
_askText_<br>
_askImage_<br>

Following method(s) return single _InstructionWithResult_ instance:<br>
_askTableFromImage_<br>
_classifyText_<br>
_classifyImage_<br>

Following methods query multiple images (document images), hence return the list of [_DocumentImageResult_](/src/main/kotlin/org/tamedai/perceptorclient/ExternalModels.kt) instances, containing,
beside the _InstructionWithResult_ list, also the original page info:<br>
_askDocumentImagePaths_<br>
_askDocumentImageStreams_<br>
_askDocumentImageBytes_<br>

