import java.util.Scanner;


public class Calculator {
    public static void main(String[] args){
        //asks the user for two numbers and an operator (+ - * /)
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your operator (+ - * /) : ");
        String operator = scanner.nextLine();
        System.out.println("Enter your first number : ");
        int num1 = scanner.nextInt();
        System.out.println("Enter your second number : ");
        int num2 = scanner.nextInt();
        
        //Validates input (no division by zero, no invalid operators)
        int result = 0;
        //Loops until the user types exit
        if (num1 >= 0 && num2 >= 0){
        switch(operator){
            case "+":
                result = num1 + num2;
                System.out.println(num1 + "+" + num2 + "=" + result);
                break;

            case "-":
                result = num1 - num2;
                System.out.println(num1 + "-" + num2 + "=" + result);
                break;
            
            case "*":
                result = num1 * num2;
                System.out.println(num1 + "*" + num2 + "=" + result);
                break;
            
            case "/":
                result = num1 / num2;
                System.out.println(num1 + "/" + num2 + "=" + result);
                break;
            
            default:
                System.out.println("The operator you entered is invalid. Please enter (+ - * /) ");
        }
        }else{
            System.out.println("The numbers you entered are invalid. Please enter positive numbers only.");
        }
        

    }
    
}