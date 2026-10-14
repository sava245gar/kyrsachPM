import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Main {

    static String usersData = ""; // все логины и пароли одной строкой

    public static void main(String[] args) {
        loadUsers();
        SwingUtilities.invokeLater(() -> loginWindow());
    }

    // ===== ОКНО ВХОДА =====
    static void loginWindow() {
        JFrame f = new JFrame("Вход");
        f.setSize(320, 230);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JTextField login = new JTextField();
        JPasswordField pass = new JPasswordField();

        p.add(new JLabel("Логин:"));
        p.add(login);
        p.add(new JLabel("Пароль:"));
        p.add(pass);

        // чекбокс "Показать пароль"
        JCheckBox showPass = new JCheckBox("Показать пароль");
        showPass.addActionListener(e -> {
            if (showPass.isSelected()) {
                pass.setEchoChar((char) 0); // показываем
            } else {
                pass.setEchoChar('•'); // скрываем
            }
        });

        JPanel buttons = new JPanel();
        JButton enterBtn = new JButton("Войти");
        JButton regBtn = new JButton("Регистрация");
        buttons.add(enterBtn);
        buttons.add(regBtn);

        enterBtn.addActionListener(e -> {
            String l = login.getText();
            String pw = new String(pass.getPassword());
            if (usersData.contains(l + ";" + pw + "\n") || usersData.endsWith(l + ";" + pw)) {
                f.dispose();
                mainWindow(l);
            } else {
                JOptionPane.showMessageDialog(f, "Неверный логин или пароль!");
            }
        });

        regBtn.addActionListener(e -> {
            f.dispose();
            registerWindow();
        });

        f.add(p, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(showPass, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.SOUTH);
        f.add(bottom, BorderLayout.SOUTH);

        f.setVisible(true);
    }

    // ===== ОКНО РЕГИСТРАЦИИ =====
    static void registerWindow() {
        JFrame f = new JFrame("Регистрация");
        f.setSize(340, 260);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel(new GridLayout(6, 1, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JTextField login = new JTextField();
        JPasswordField pass1 = new JPasswordField();
        JPasswordField pass2 = new JPasswordField();

        p.add(new JLabel("Логин:"));
        p.add(login);
        p.add(new JLabel("Пароль:"));
        p.add(pass1);
        p.add(new JLabel("Подтверждение пароля:"));
        p.add(pass2);

        // чекбокс "Показать пароли"
        JCheckBox showPass = new JCheckBox("Показать пароли");
        showPass.addActionListener(e -> {
            if (showPass.isSelected()) {
                pass1.setEchoChar((char) 0);
                pass2.setEchoChar((char) 0);
            } else {
                pass1.setEchoChar('•');
                pass2.setEchoChar('•');
            }
        });

        JPanel buttons = new JPanel();
        JButton createBtn = new JButton("Создать");
        JButton backBtn = new JButton("Назад");
        buttons.add(createBtn);
        buttons.add(backBtn);

        createBtn.addActionListener(e -> {
            String l = login.getText();
            String p1 = new String(pass1.getPassword());
            String p2 = new String(pass2.getPassword());

            if (l.length() < 3 || p1.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Логин минимум 3 символа!");
                return;
            }
            if (!p1.equals(p2)) {
                JOptionPane.showMessageDialog(f, "Пароли не совпадают!");
                return;
            }
            if (usersData.contains(l + ";")) {
                JOptionPane.showMessageDialog(f, "Логин занят!");
                return;
            }
            if (checkPassword(p1).contains("СЛАБЫЙ")) {
                int c = JOptionPane.showConfirmDialog(f, "Пароль слабый. Продолжить?");
                if (c != 0) return;
            }

            usersData += l + ";" + p1 + "\n";
            saveUsers();
            JOptionPane.showMessageDialog(f, "Готово!");
            f.dispose();
            loginWindow();
        });

        backBtn.addActionListener(e -> {
            f.dispose();
            loginWindow();
        });

        f.add(p, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(showPass, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.SOUTH);
        f.add(bottom, BorderLayout.SOUTH);

        f.setVisible(true);
    }

    // ===== ГЛАВНОЕ ОКНО =====
    static void mainWindow(String username) {
        JFrame f = new JFrame("Проверка пароля — " + username);
        f.setSize(400, 350);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPasswordField pass = new JPasswordField();
        JTextArea result = new JTextArea();
        result.setEditable(false);
        result.setLineWrap(true);

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(new JLabel("Пароль:"), BorderLayout.WEST);
        top.add(pass, BorderLayout.CENTER);

        JButton logout = new JButton("Выйти");
        logout.addActionListener(e -> {
            f.dispose();
            loginWindow();
        });

        // проверка при вводе
        pass.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            void update() {
                result.setText(checkPassword(new String(pass.getPassword())));
            }
        });

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(result), BorderLayout.CENTER);
        p.add(logout, BorderLayout.SOUTH);
        f.add(p);
        f.setVisible(true);
    }

    // ===== ПРОВЕРКА ПАРОЛЯ =====
    static String checkPassword(String p) {
        if (p.isEmpty()) return "Введите пароль";

        int score = 0;
        if (p.length() >= 8) score++;
        if (p.length() >= 12) score++;
        if (p.matches(".*[a-z].*")) score++;
        if (p.matches(".*[A-Z].*")) score++;
        if (p.matches(".*\\d.*")) score++;
        if (p.matches(".*[^A-Za-z0-9].*")) score++;

        String level;
        if (score <= 2) level = "СЛАБЫЙ";
        else if (score <= 4) level = "СРЕДНИЙ";
        else level = "НАДЁЖНЫЙ";

        String tips = "";
        if (p.length() < 8) tips += "• Минимум 8 символов\n";
        if (!p.matches(".*[a-z].*")) tips += "• Добавьте строчные буквы\n";
        if (!p.matches(".*[A-Z].*")) tips += "• Добавьте заглавные буквы\n";
        if (!p.matches(".*\\d.*")) tips += "• Добавьте цифры\n";
        if (!p.matches(".*[^A-Za-z0-9].*")) tips += "• Добавьте спецсимволы\n";
        if (score == 6) tips = "Отличный пароль!";

        return level + " ПАРОЛЬ\n\nОценка: " + score + "/6\n\n" + tips;
    }

    // ===== ФАЙЛ =====
    static void loadUsers() {
        try {
            File file = new File("users.txt");
            if (!file.exists()) return;
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line;
            while ((line = r.readLine()) != null) {
                usersData += line + "\n";
            }
            r.close();
        } catch (Exception e) { }
    }

    static void saveUsers() {
        try {
            FileWriter w = new FileWriter("users.txt");
            w.write(usersData);
            w.close();
        } catch (Exception e) { }
    }
}