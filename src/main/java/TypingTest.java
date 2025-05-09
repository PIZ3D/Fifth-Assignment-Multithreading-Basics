import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class TypingTest {
    private static volatile String lastInput = "";
    private static Scanner scanner = new Scanner(System.in);
    private static int correctCount = 0;
    private static int incorrectCount = 0;
    private static long startTime;
    private static volatile boolean running = true;

    public static class InputRunnable implements Runnable {
        @Override
        public void run() {
            try {
                if (scanner.hasNextLine()) {
                    lastInput = scanner.nextLine();
                }
            } catch (Exception e) {
                // Ignore scanner closed exception
            }
        }
    }

    public static void testWord(String wordToTest) {
        try {
            System.out.println("Type this word: " + wordToTest);
            lastInput = "";

            // Calculate (2 seconds per character)
            long timeout = wordToTest.length() * 2000L;

            Thread inputThread = new Thread(new InputRunnable());
            inputThread.start();

            long wordStartTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - wordStartTime < timeout && lastInput.isEmpty()) {
                Thread.sleep(100); // Check every 100ms
            }

            if (inputThread.isAlive()) {
                inputThread.interrupt();
                if (lastInput.isEmpty()) {
                    System.out.println("\nTime's up!");
                }
            }

            System.out.println("You typed: " + (lastInput.isEmpty() ? "[nothing]" : lastInput));
            if (lastInput.equals(wordToTest)) {
                System.out.println("Correct");
                correctCount++;
            } else {
                System.out.println("Incorrect");
                incorrectCount++;
            }

        } catch (Exception e) {
            System.out.println("Error during test: " + e.getMessage());
        }
    }

    public static void typingTest(List<String> inputList) throws InterruptedException {
        correctCount = 0;
        incorrectCount = 0;
        startTime = System.currentTimeMillis();
        running = true;

        for (String wordToTest : inputList) {
            if (!running) break;
            testWord(wordToTest);
            Thread.sleep(1000);
        }

        // Display
        long totalTime = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("\n--- Test Results ---");
        System.out.println("Total words: " + inputList.size());
        System.out.println("Correct: " + correctCount);
        System.out.println("Incorrect: " + incorrectCount);
        System.out.println("Total time: " + totalTime + " seconds");
        if (correctCount + incorrectCount > 0) {
            System.out.println("Average time per word: " +
                    (totalTime / (correctCount + incorrectCount)) + " seconds");
        }
    }

    public static List<String> loadWordsFromResources() {
        List<String> words = new ArrayList<>();
        try {
            InputStream is = TypingTest.class.getClassLoader().getResourceAsStream("words.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error loading words file: " + e.getMessage());
            words.add("error");
            words.add("loading");
            words.add("words");
            words.add("file");
        }
        return words;
    }

    public static void main(String[] args) throws InterruptedException {

        List<String> words = loadWordsFromResources();


        Collections.shuffle(words, new Random());
        List<String> testWords = words.subList(0, Math.min(10, words.size()));

        typingTest(testWords);

        System.out.println("Press enter to exit.");
        scanner.nextLine();
        scanner.close();
    }
}