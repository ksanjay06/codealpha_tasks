import java.util.ArrayList;
import java.util.Scanner;

class Student {
    String name;
    double grade;

    Student(String name, double grade) {
        this.name = name;
        this.grade = grade;
    }
}

public class StudentGradeTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Student> students = new ArrayList<>();

        System.out.println("===== Student Grade Tracker =====");

        System.out.print("Enter number of students: ");
        int n = scanner.nextInt();
        scanner.nextLine(); 

        
        for (int i = 0; i < n; i++) {
            System.out.println("\nStudent " + (i + 1));

            System.out.print("Enter student name: ");
            String name = scanner.nextLine();

            System.out.print("Enter grade: ");
            double grade = scanner.nextDouble();
            scanner.nextLine();

            students.add(new Student(name, grade));
        }
      
        double total = 0;
        double highest = students.get(0).grade;
        double lowest = students.get(0).grade;

        for (Student student : students) {
            total += student.grade;

            if (student.grade > highest) {
                highest = student.grade;
            }

            if (student.grade < lowest) {
                lowest = student.grade;
            }
        }

        double average = total / students.size();

      
        System.out.println("\n===== Summary Report =====");
        System.out.printf("%-20s %-10s\n", "Student Name", "Grade");
        System.out.println("--------------------------------");

        for (Student student : students) {
            System.out.printf("%-20s %-10.2f\n",
                    student.name, student.grade);
        }

        System.out.println("--------------------------------");
        System.out.printf("Average Score : %.2f\n", average);
        System.out.printf("Highest Score : %.2f\n", highest);
        System.out.printf("Lowest Score  : %.2f\n", lowest);

        scanner.close();
    }
}
