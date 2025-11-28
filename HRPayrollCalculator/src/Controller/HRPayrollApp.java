package Controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Employee {
    private int id;
    private String name;
    private double basicSalary;

    public Employee(int id, String name, double basicSalary) {
        this.id = id;
        this.name = name;
        this.basicSalary = basicSalary;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public String toCsv() {
        return id + "," + name + "," + basicSalary;
    }

    public static Employee fromCsv(String line) {
        String[] parts = line.split(",");
        if (parts.length != 3) {
            return null;
        }
        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            double basic = Double.parseDouble(parts[2].trim());
            return new Employee(id, name, basic);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

class PayrollCalculator {
    private double hraPercent = 20.0;
    private double daPercent = 10.0;
    private double pfPercent = 8.0;

    public PayrollResult calculate(double basicSalary) {
        double hra = basicSalary * hraPercent / 100.0;
        double da = basicSalary * daPercent / 100.0;
        double gross = basicSalary + hra + da;
        double pf = basicSalary * pfPercent / 100.0;
        double net = gross - pf;
        return new PayrollResult(basicSalary, hra, da, pf, gross, net);
    }
}

class PayrollResult {
    private double basic;
    private double hra;
    private double da;
    private double pf;
    private double gross;
    private double net;

    public PayrollResult(double basic, double hra, double da, double pf, double gross, double net) {
        this.basic = basic;
        this.hra = hra;
        this.da = da;
        this.pf = pf;
        this.gross = gross;
        this.net = net;
    }

    public double getBasic() {
        return basic;
    }

    public double getHra() {
        return hra;
    }

    public double getDa() {
        return da;
    }

    public double getPf() {
        return pf;
    }

    public double getGross() {
        return gross;
    }

    public double getNet() {
        return net;
    }
}

class FileStorage {
    private String employeeFile;
    private String payrollFile;

    public FileStorage(String employeeFile, String payrollFile) {
        this.employeeFile = employeeFile;
        this.payrollFile = payrollFile;
    }

    public void saveEmployee(Employee e) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(employeeFile, true))) {
            bw.write(e.toCsv());
            bw.newLine();
        }
    }

    public List<Employee> loadEmployees() throws IOException {
        List<Employee> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(employeeFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                Employee e = Employee.fromCsv(line);
                if (e != null) {
                    list.add(e);
                }
            }
        } catch (IOException e) {
            throw e;
        }
        return list;
    }

    public void savePayroll(Employee e, PayrollResult pr, String month) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(payrollFile, true))) {
            String row = month + "," +
                         e.getId() + "," +
                         e.getName() + "," +
                         pr.getBasic() + "," +
                         pr.getHra() + "," +
                         pr.getDa() + "," +
                         pr.getPf() + "," +
                         pr.getGross() + "," +
                         pr.getNet();
            bw.write(row);
            bw.newLine();
        }
    }
}

public class HRPayrollApp {

    private static final String EMP_FILE = "employees.txt";
    private static final String PAYROLL_FILE = "payroll.txt";

    private static Scanner sc = new Scanner(System.in);
    private static FileStorage storage = new FileStorage(EMP_FILE, PAYROLL_FILE);
    private static PayrollCalculator calculator = new PayrollCalculator();

    public static void main(String[] args) {

        boolean running = true;
        while (running) {
            showMenu();
            int choice = getIntInput("Enter your choice: ");
            switch (choice) {
                case 1:
                    addEmployee();
                    break;
                case 2:
                    listEmployees();
                    break;
                case 3:
                    generatePayroll();
                    break;
                case 4:
                    System.out.println("Exiting HR Payroll Application.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Enter 1, 2, 3 or 4.");
            }
            System.out.println();
        }

        sc.close();
    }

    private static void showMenu() {
        System.out.println("=================================");
        System.out.println(" HR Payroll Calculator");
        System.out.println("=================================");
        System.out.println("1. Add Employee");
        System.out.println("2. List Employees");
        System.out.println("3. Generate Payroll for a Month");
        System.out.println("4. Exit");
        System.out.println("=================================");
    }

    private static int getIntInput(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                String line = sc.nextLine();
                int value = Integer.parseInt(line);
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    private static double getDoubleInput(String msg) {
        while (true) {
            try {
                System.out.print(msg);
                String line = sc.nextLine();
                double value = Double.parseDouble(line);
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    private static String getStringInput(String msg) {
        System.out.print(msg);
        return sc.nextLine();
    }

    private static void addEmployee() {
        try {
            int id = getIntInput("Enter employee id: ");
            String name = getStringInput("Enter employee name: ");
            double basic = getDoubleInput("Enter basic salary: ");

            Employee e = new Employee(id, name, basic);
            storage.saveEmployee(e);
            System.out.println("Employee saved to file " + EMP_FILE);
        } catch (IOException e) {
            System.out.println("Error saving employee: " + e.getMessage());
        }
    }

    private static void listEmployees() {
        try {
            List<Employee> list = storage.loadEmployees();
            if (list.isEmpty()) {
                System.out.println("No employees found.");
                return;
            }
            System.out.println("ID\tName\tBasic Salary");
            for (Employee e : list) {
                System.out.println(e.getId() + "\t" + e.getName() + "\t" + e.getBasicSalary());
            }
        } catch (IOException e) {
            System.out.println("Error reading employees: " + e.getMessage());
        }
    }

    private static void generatePayroll() {
        try {
            List<Employee> list = storage.loadEmployees();
            if (list.isEmpty()) {
                System.out.println("No employees available to generate payroll.");
                return;
            }

            String month = getStringInput("Enter month (e.g. 2025-11): ");

            for (Employee e : list) {
                PayrollResult pr = calculator.calculate(e.getBasicSalary());
                storage.savePayroll(e, pr, month);
            }

            System.out.println("Payroll generated and saved to file " + PAYROLL_FILE);

        } catch (IOException e) {
            System.out.println("Error during payroll generation: " + e.getMessage());
        }
    }
}
