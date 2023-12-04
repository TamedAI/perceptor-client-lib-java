# Perceptor Java Client

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
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
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

        var res = client .askText(textToProcess,
                instructions
        );

        InstructionWithResponse firstResp = res.get(0);

        var s = (PerceptorSuccessResult)firstResp.getResponse();

        System.out.println("Got response:");
        System.out.println(s.getAnswer());

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

        var responseImage = client.askImage("path_to_image_file", 
                Arrays.asList("What is the invoice number?",
                            "What is the invoice date?"
                )
        );

        var firstResponseToImage  = (PerceptorSuccessResult)responseImage.get(0).getResponse();

        System.out.println("Got response:");
        System.out.println(firstResponseToImage.getAnswer());

    }
}
```



