package ch06.inheritance;

public class InheritanceDemo {

    static class Employee {                               // superclass (parent, base class)
        private final String name;                        // private: NOT accessible in subclasses
        protected double baseSalary;                      // protected: accessible in subclasses

        Employee(String name, double baseSalary) {
            this.name = name;
            this.baseSalary = baseSalary;
        }
        String getName() { return name; }
        double pay() { return baseSalary; }
        String describe() { return getName() + " earns " + pay(); }
    }

    static class Manager extends Employee {               // subclass (child, derived class): Manager IS-AN Employee
        private final double bonus;

        Manager(String name, double baseSalary, double bonus) {
            super(name, baseSalary);                      // must initialise the Employee part first
            this.bonus = bonus;
        }
        @Override
        double pay() { return super.pay() + bonus; }      // reuse the parent version, then extend it

        void approveBudget() { System.out.println(getName() + " approved the budget"); } // new behaviour
    }

    static class Intern extends Employee {
        Intern(String name) { super(name, 1_000); }       // no pay() override: inherits Employee.pay()
    }

    public static void main(String[] args) {
        Employee e = new Employee("Ana", 3_000);
        Manager m = new Manager("Bo", 5_000, 1_500);
        Intern i = new Intern("Cy");
        System.out.println(e.describe());
        System.out.println(m.describe());                 // inherited describe() calls the overridden pay()
        System.out.println(i.describe());
        m.approveBudget();
        System.out.println("m instanceof Employee? " + (m instanceof Employee)
                + ", superclass of Manager: " + Manager.class.getSuperclass().getSimpleName()
                + ", superclass of Employee: " + Employee.class.getSuperclass().getSimpleName());
    }
}
