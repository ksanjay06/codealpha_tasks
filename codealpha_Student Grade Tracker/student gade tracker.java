import java.util.ArrayList;
import java.util.Scanner;


public class StudentGradeTracker {

    
    static class Student {
        String name;
        ArrayList<Double> scores = new ArrayList<>();

        Student(String name) {
            this.name = name;
        }

        double getAverage() {
            if (scores.isEmpty()) return 0.0;
            double sum = 0;
            for (double s : scores) sum += s;
            return sum / scores.size();
        }

        double getHighest() {
            double max = scores.get(0);
            for (double s : scores) if (s > max) max = s;
            return max;
        }

        double getLowest() {
            double min = scores.get(0);
            for (double s : scores) if (s < min) min = s;
            return min;
        }

        String getGrade() {
            double avg = getAverage();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }
    }

    private static final ArrayList<Student> students = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("=========================================");
        System.out.println("       STUDENT GRADE TRACKER");
        System.out.println("=========================================");

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addStudent();
                    break;
                case "2":
                    addScoreToStudent();
                    break;
                case "3":
                    displaySummaryReport();
                    break;
                case "4":
                    displayClassStatistics();
                    break;
                case "5":
                    removeStudent();
                    break;
                case "6":
                    running = false;
                    System.out.println("Exiting... Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.\n");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n----------------- MENU -----------------");
        System.out.println("1. Add new student");
        System.out.println("2. Add score to a student");
        System.out.println("3. Display summary report (all students)");
        System.out.println("4. Display class statistics");
        System.out.println("5. Remove a student");
        System.out.println("6. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void addStudent() {
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        if (findStudent(name) != null) {
            System.out.println("Student already exists.");
            return;
        }

        Student student = new Student(name);
        System.out.print("How many scores would you like to enter? ");
        int count = readInt();

        for (int i = 0; i < count; i++) {
            double score = readScore("Enter score #" + (i + 1) + " for " + name + ": ");
            student.scores.add(score);
        }

        students.add(student);
        System.out.println("Student \"" + name + "\" added with " + count + " score(s).");
    }

    private static void addScoreToStudent() {
        if (students.isEmpty()) {
            System.out.println("No students found. Add a student first.");
            return;
        }
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        Student student = findStudent(name);

        if (student == null) {
            System.out.println("Student not found.");
            return;
        }

        double score = readScore("Enter score to add for " + student.name + ": ");
        student.scores.add(score);
        System.out.println("Score added.");
    }

    private static void removeStudent() {
        if (students.isEmpty()) {
            System.out.println("No students to remove.");
            return;
        }
        System.out.print("Enter student name to remove: ");
        String name = scanner.nextLine().trim();
        Student student = findStudent(name);

        if (student == null) {
            System.out.println("Student not found.");
            return;
        }
        students.remove(student);
        System.out.println("Student \"" + name + "\" removed.");
    }

    private static void displaySummaryReport() {
        if (students.isEmpty()) {
            System.out.println("No students to display.");
            return;
        }

        System.out.println("\n========================= SUMMARY REPORT =========================");
        System.out.printf("%-15s %-10s %-10s %-10s %-8s %-8s%n",
                "Name", "Avg", "Highest", "Lowest", "Grade", "#Scores");
        System.out.println("--------------------------------------------------------------------");

        for (Student s : students) {
            if (s.scores.isEmpty()) {
                System.out.printf("%-15s %-10s %-10s %-10s %-8s %-8d%n",
                        s.name, "-", "-", "-", "-", 0);
            } else {
                System.out.printf("%-15s %-10.2f %-10.2f %-10.2f %-8s %-8d%n",
                        s.name, s.getAverage(), s.getHighest(), s.getLowest(),
                        s.getGrade(), s.scores.size());
            }
        }
        System.out.println("====================================================================");
    }

    private static void displayClassStatistics() {
        ArrayList<Double> allScores = new ArrayList<>();
        for (Student s : students) {
            allScores.addAll(s.scores);
        }

        if (allScores.isEmpty()) {
            System.out.println("No scores recorded yet.");
            return;
        }

        double sum = 0, max = allScores.get(0), min = allScores.get(0);
        for (double score : allScores) {
            sum += score;
            if (score > max) max = score;
            if (score < min) min = score;
        }
        double classAverage = sum / allScores.size();

        System.out.println("\n------------- CLASS STATISTICS -------------");
        System.out.println("Total students     : " + students.size());
        System.out.println("Total scores logged: " + allScores.size());
        System.out.printf("Class average      : %.2f%n", classAverage);
        System.out.printf("Highest score (all) : %.2f%n", max);
        System.out.printf("Lowest score (all)  : %.2f%n", min);
        System.out.println("---------------------------------------------");
    }

    private static Student findStudent(String name) {
        for (Student s : students) {
            if (s.name.equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number, try again: ");
            }
        }
    }

    private static double readScore(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double val = Double.parseDouble(scanner.nextLine().trim());
                if (val < 0 || val > 100) {
                    System.out.println("Score must be between 0 and 100.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid score, please enter a number.");
            }
        }
    }
}
