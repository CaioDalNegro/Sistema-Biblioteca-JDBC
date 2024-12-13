package biblioteca;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import db.DB;

public class Biblioteca extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField tituloBi;
    private JTextField textField;
    private JButton btnEmprestar;
    private JButton btnDevolver;
    private JLabel lblUsuarioStatus;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Biblioteca frame = new Biblioteca();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Biblioteca() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 600);
        setTitle("Biblioteca Memphis");
        setResizable(false);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        contentPane.setBackground(new Color(240, 248, 255));

        tituloBi = new JTextField();
        tituloBi.setBounds(250, 20, 400, 50);
        tituloBi.setHorizontalAlignment(SwingConstants.CENTER);
        tituloBi.setFont(new Font("Segoe UI", Font.BOLD, 28));
        tituloBi.setText("BIBLIOTECA MEMPHIS");
        tituloBi.setEditable(false);
        tituloBi.setBackground(new Color(135, 206, 250));
        tituloBi.setForeground(new Color(25, 25, 112));
        contentPane.add(tituloBi);

        textField = new JTextField();
        textField.setBackground(new Color(192, 192, 192));
        textField.setEditable(false);
        textField.setBounds(27, 90, 547, 130);
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        contentPane.add(textField);

        lblUsuarioStatus = new JLabel("<html><b>Usuário: Nenhum</b><br/>Status: Nenhum empréstimo</html>");
        lblUsuarioStatus.setFont(new Font("Arial", Font.PLAIN, 16));
        lblUsuarioStatus.setBounds(600, 90, 250, 60);
        contentPane.add(lblUsuarioStatus);

        btnEmprestar = new JButton("Emprestar");
        btnEmprestar.setFont(new Font("Tahoma", Font.PLAIN, 18));
        btnEmprestar.setBounds(86, 234, 157, 55);
        btnEmprestar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String usuario = JOptionPane.showInputDialog("Digite o nome do usuário:");
                String isbn = JOptionPane.showInputDialog("Digite o ISBN do livro:");

                if (!isUsuarioValido(usuario)) {
                    JOptionPane.showMessageDialog(null, "Esse usuário não existe.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!isIsbnValido(isbn)) {
                    JOptionPane.showMessageDialog(null, "Esse ISBN não existe.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (realizarEmprestimo(usuario, isbn)) {
                    textField.setText("Livro com ISBN " + isbn + " emprestado para " + usuario);
                    lblUsuarioStatus.setText("<html><b>Usuário: " + usuario + "</b><br/>Status: Livro emprestado com ISBN " + isbn + "</html>");
                }
            }
        });
        contentPane.add(btnEmprestar);

        btnDevolver = new JButton("Devolver");
        btnDevolver.setFont(new Font("Tahoma", Font.PLAIN, 18));
        btnDevolver.setBounds(305, 234, 157, 55);
        btnDevolver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String usuario = JOptionPane.showInputDialog("Digite o nome do usuário:");
                String isbn = JOptionPane.showInputDialog("Digite o ISBN do livro:");

                if (!isUsuarioValido(usuario)) {
                    JOptionPane.showMessageDialog(null, "Esse usuário não existe.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!isIsbnValido(isbn)) {
                    JOptionPane.showMessageDialog(null, "Esse ISBN não existe.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (realizarDevolucao(usuario, isbn)) {
                    textField.setText("Livro com ISBN " + isbn + " devolvido por " + usuario);
                    lblUsuarioStatus.setText("<html><b>Usuário: " + usuario + "</b><br/>Status: Livro devolvido com ISBN " + isbn + "</html>");
                }
            }
        });
        contentPane.add(btnDevolver);
    }

    private boolean realizarEmprestimo(String usuario, String isbn) {
        Connection conn = null;
        PreparedStatement stLivro = null;
        PreparedStatement stEmprestimo = null;

        try {
            conn = DB.getConnection();

            // Buscar número de registro do usuário
            int numeroRegistro = buscarNumeroRegistro(usuario);
            if (numeroRegistro == -1) {
                JOptionPane.showMessageDialog(null, "Usuário inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Atualizar a tabela 'livro'
            String sqlLivro = "UPDATE livro SET Exemplar = Exemplar - 1, Disponibilidade = (Exemplar > 1) WHERE ISBN = ? AND Exemplar > 0";
            stLivro = conn.prepareStatement(sqlLivro);
            stLivro.setString(1, isbn);
            int rowsUpdated = stLivro.executeUpdate();

            if (rowsUpdated == 0) {
                JOptionPane.showMessageDialog(null, "Livro indisponível para empréstimo!", "Erro", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // Atualizar a tabela 'emprestimo'
            String sqlEmprestimo = "INSERT INTO emprestimo (Livro_ISBN, Usuario_NumeroRegistro, Data_emprestimo, Data_devolucao) VALUES (?, ?, NOW(), NULL)";
            stEmprestimo = conn.prepareStatement(sqlEmprestimo);
            stEmprestimo.setString(1, isbn);
            stEmprestimo.setInt(2, numeroRegistro);
            stEmprestimo.executeUpdate();

            JOptionPane.showMessageDialog(null, "Empréstimo realizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao realizar o empréstimo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            DB.closeStatement(stLivro);
            DB.closeStatement(stEmprestimo);
        }
    }

    private boolean realizarDevolucao(String usuario, String isbn) {
        Connection conn = null;
        PreparedStatement stLivro = null;
        PreparedStatement stEmprestimo = null;

        try {
            conn = DB.getConnection();

            // Atualizar a tabela 'livro'
            String sqlLivro = "UPDATE livro SET Exemplar = Exemplar + 1, Disponibilidade = true WHERE ISBN = ?";
            stLivro = conn.prepareStatement(sqlLivro);
            stLivro.setString(1, isbn);
            stLivro.executeUpdate();

            // Atualizar a tabela 'emprestimo'
            String sqlEmprestimo = "DELETE FROM emprestimo WHERE Livro_ISBN = ? AND Usuario_NumeroRegistro = (SELECT NumeroRegistro FROM usuario WHERE Nome = ?) LIMIT 1";
            stEmprestimo = conn.prepareStatement(sqlEmprestimo);
            stEmprestimo.setString(1, isbn);
            stEmprestimo.setString(2, usuario);
            stEmprestimo.executeUpdate();

            JOptionPane.showMessageDialog(null, "Devolução realizada com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro ao realizar a devolução: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            DB.closeStatement(stLivro);
            DB.closeStatement(stEmprestimo);
        }
    }

    private int buscarNumeroRegistro(String usuario) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();
            String sql = "SELECT NumeroRegistro FROM usuario WHERE Nome = ?";
            st = conn.prepareStatement(sql);
            st.setString(1, usuario);
            rs = st.executeQuery();

            if (rs.next()) {
                return rs.getInt("NumeroRegistro");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    private boolean isUsuarioValido(String usuario) {
        return buscarNumeroRegistro(usuario) != -1;
    }

    private boolean isIsbnValido(String isbn) {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            conn = DB.getConnection();
            String sql = "SELECT ISBN FROM livro WHERE ISBN = ?";
            st = conn.prepareStatement(sql);
            st.setString(1, isbn);
            rs = st.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }
}
