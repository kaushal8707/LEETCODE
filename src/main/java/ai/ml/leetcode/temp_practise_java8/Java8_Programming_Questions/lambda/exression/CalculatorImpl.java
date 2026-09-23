package ai.ml.leetcode.temp_practise_java8.Java8_Programming_Questions.lambda.exression;

public class CalculatorImpl //implements Calculator
{

	
	
	//Traditional Approach
//	@Override
//	public void switchOn() {
//		System.out.println("Calculator switched on"); 
//	}
//	
	public static void main(String[] args) {
		//new CalculatorImpl().switchOn();
		
		//using lambda expression
		
//		Calculator calc = ()->System.out.println("Invoke switched on using lambda");
//		calc.switchOn();
		
		Calculator calcParam = (d)->{
			if(d>10)
				System.out.println("greater than");
			else
				System.out.println("less than");
		};
		calcParam.printNum(1);
		
	}
	
}
