//Program picks a random number 1–100
//User guesses, program says "too high" or "too low"
//Counts attempts, prints "You got it in X tries"

import java.util.Scanner;

public class NumberGuessingGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int randomNumber = (int) (Math.random() * 100 + 1);
        int guess = 0;
        int attempts =0;

        System.out.println("Welcome to the Number Guessing Game!");
        System.out.println("I have picked a random number between 1 and 100. Try to guess it!");

        while (guess != randomNumber){
            System.out.println("Enter the number you guessed)");
            guess = scanner.nextInt();
            attempts++;

            if (guess < randomNumber){
                System.out.println("Too low! Try again.");
            } else if (guess > randomNumber){
                System.out.println("Too high! Try again.");
            } else {
                System.out.println("Congratulations! You guessed the number " + randomNumber + " in " + attempts + " attempts.");
            }
        }
    }
}
