import com.student.Student;
import com.student.StudentService;

public class Main {

    public static void main(String[] args) {

        StudentService service = new StudentService();

        Student student = new Student(
                "SV001",
                "Nguyen Van An",
                8.5
        );

        System.out.println("Add: "
                + service.addStudent(student));

        System.out.println("Find: "
                + service.findStudentById("SV001"));

        System.out.println("Update: "
                + service.updateStudent(
                "SV001",
                "Nguyen Van An",
                9.0
        ));

        System.out.println("After update: "
                + service.findStudentById("SV001"));

        System.out.println("Delete: "
                + service.deleteStudent("SV001"));

        System.out.println("After delete: "
                + service.findStudentById("SV001"));
    }
}