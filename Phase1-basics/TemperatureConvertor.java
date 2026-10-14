//Convert Celsius ↔ Fahrenheit

//Menu-driven (user chooses direction)

//Handles invalid input gracefully

import java.util.Scanner;

public class TemperatureConvertor {
    public static void main(String[] args){
        System.out.println("Welcom to the Temperature Convertor!");
        System.out.println("Please choose the conversion direction:(1 or 2)");
        System.out.println("Option 1. Celsius to Fahrenheit");
        System.out.println("Option 2. Fahrenheit to Celsius");

        int option = 0;
        Scanner scanner = new Scanner(System.in);
        option = scanner.nextInt();

        if (option == 1) {
            System.out.println("Enter the temperature in Celsius: ");
            double celsius = scanner.nextDouble();
            System.out.println("Your "+ celsius + "°C is equal to " + (celsius * 9/5 + 32) + "°F");
        }else if (option == 2) {
            System.out.println("Enter the temperature in Fahrenheit: ");
            double fahrenheit = scanner.nextDouble();
            System.out.println("Your "+ fahrenheit + "°F is equal to " + ((fahrenheit - 32) * 5/9) + "°C");
        }else{
            System.out.println("Invalid option. Please choose 1 or 2.");
        }
    }
}