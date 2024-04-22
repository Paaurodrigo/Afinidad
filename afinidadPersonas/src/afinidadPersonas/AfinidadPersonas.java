package afinidadPersonas;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JCheckBox;



public class AfinidadPersonas {

	private JFrame frame;
	private JTextField txtNombre;
	private JTextField txtApellidos;
	private JTextField txtFechaNac;
	private JTextField txtFoto;
	private JTextField txtId;
	private JTextField txtEdad;
	
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AfinidadPersonas window = new AfinidadPersonas();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void mostrarPersona(DefaultTableModel modelUsuario) {
		if (modelUsuario.getRowCount() > 0) {
			modelUsuario.setRowCount(0);
		}
		try {
			Connection con = ConnectionSingleton.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Personas");
			while (rs.next()) {
				Object[] row = new Object[5];
				row[0] = rs.getInt("id");
				row[1] = rs.getString("nombre");
				row[2] = rs.getString("apellidos");
				row[3] = rs.getString("fecha_nacimiento");
				row[4] = rs.getInt("edad");
				;

				modelUsuario.addRow(row);

			}
			rs.close();
			stmt.close();
			con.close();

		} catch (SQLException ex) { // Caso erróneo
			JOptionPane.showMessageDialog(null, "No se a podido cargar los datos/n" + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * Create the application.
	 */
	public AfinidadPersonas() {
		initialize();
		// Tabla afiliados
		
        DefaultTableModel modelAfiliados = new DefaultTableModel();
        modelAfiliados.addColumn("Nombre");       
        JTable tableAfiliados = new JTable(modelAfiliados);
        JScrollPane scrollPaneAfiliados = new JScrollPane(tableAfiliados);
        scrollPaneAfiliados.setBounds(687, 395, 216, 225);
        frame.getContentPane().add(scrollPaneAfiliados);
		
		
		//Tabla personas
		
		DefaultTableModel modelUsuario = new DefaultTableModel();
		
		modelUsuario.addColumn("ID Usuario");
		modelUsuario.addColumn("Nombre");
		modelUsuario.addColumn("Apellidos");
		modelUsuario.addColumn("Fecha Nacimiento");
		modelUsuario.addColumn("Edad");
		
		
		
		

		// Checkbox hobies
		 JCheckBox[] checkboxesHobbies = new JCheckBox[8];
	        String[] nombresHobbies = {"Fútbol", "Pintar", "Escalada", "Baile", "Pesca", "Lectura", "Coleccionismo", "Fotografía"};

		

		 int y = 330;

	        for (int i = 0; i < checkboxesHobbies.length; i++) {
	            checkboxesHobbies[i] = new JCheckBox(nombresHobbies[i]);
	            checkboxesHobbies[i].setBounds(20, y, 150, 20); 
	            y += 30;
	            frame.getContentPane().add(checkboxesHobbies[i]);
	        }
			
		
		
		
		try {
			Connection con = ConnectionSingleton.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Personas");
			while (rs.next()) {
				Object[] row = new Object[5];
				row[0] = rs.getInt("id");
				row[1] = rs.getString("nombre");
				row[2] = rs.getString("apellidos");
				row[3] = rs.getString("fecha_nacimiento");
				row[4] = rs.getInt("edad");
				;

				modelUsuario.addRow(row);

			}
			
			JTable table = new JTable(modelUsuario);
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					
					int index = table.getSelectedRow();

					TableModel modelUsuario = table.getModel();
					txtId.setText(modelUsuario.getValueAt(index, 0).toString());
					txtNombre.setText(modelUsuario.getValueAt(index, 1).toString());
					txtApellidos.setText(modelUsuario.getValueAt(index, 2).toString());
					txtFechaNac.setText(modelUsuario.getValueAt(index, 3).toString());
					txtEdad.setText(modelUsuario.getValueAt(index, 4).toString());
					
					int idU=(int) (modelUsuario.getValueAt(index, 0));
					
					for (JCheckBox checkbox : checkboxesHobbies) {                  
	                        checkbox.setSelected(false);
	                    }
					 // Obtener los hobbies asociados al usuario seleccionado
			        try (Connection con = ConnectionSingleton.getConnection()) {
			            PreparedStatement pstmt = con.prepareStatement("SELECT hobbie FROM persona_hobbie WHERE persona_id = ?");
			            pstmt.setInt(1, idU);
			            ResultSet rs = pstmt.executeQuery();
			            while (rs.next()) {
			                String hobby = rs.getString("hobbie");
			                // Marcar el checkbox correspondiente al hobby
			                for (JCheckBox checkbox : checkboxesHobbies) {
			                    if (checkbox.getText().equals(hobby)) {
			                        checkbox.setSelected(true);
			                    }
			                }
			            }
			        } catch (SQLException ex) {
			            ex.printStackTrace();
			        }
			        
			        // comprobar si hay afiliados, si los hay se añaden en la tabla
			        try (Connection con = ConnectionSingleton.getConnection()) {
			        	PreparedStatement pstmt = con.prepareStatement("SELECT DISTINCT p.nombre  FROM personas p  JOIN persona_hobbie ph ON p.id = ph.persona_id  WHERE ph.hobbie IN (SELECT hobbie FROM persona_hobbie WHERE persona_id = ?)   AND p.id != ?  GROUP BY p.nombre HAVING COUNT(DISTINCT ph.hobbie) >= 3");
			        	
			        	pstmt.setInt(1, idU);
			                pstmt.setInt(2, idU);
			                ResultSet rs = pstmt.executeQuery();
			                modelAfiliados.setRowCount(0);
			                do {			                    	
			                    	while (rs.next()) {			                    	   
			                    	    String nombreAfiliado = rs.getString("nombre");
			                    	    modelAfiliados.addRow(new Object[]{nombreAfiliado});
			                    	}
			                    } while (rs.next());
			        
			        }  catch (SQLException ex) {
			            ex.printStackTrace();
			        }
			        					
				}
			});
			frame.getContentPane().setLayout(null);
			
			table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setBounds(53, 37, 658, 225);

			frame.getContentPane().add(scrollPane);
			
			JLabel lblNewLabel = new JLabel("Nombre:");
			lblNewLabel.setBounds(279, 279, 81, 16);
			frame.getContentPane().add(lblNewLabel);
			
			JLabel lblNewLabel_1 = new JLabel("Apellidos:");
			lblNewLabel_1.setBounds(542, 274, 81, 16);
			frame.getContentPane().add(lblNewLabel_1);
			
			JLabel lblFechaNacimiento = new JLabel("Fecha Nacimiento:");
			lblFechaNacimiento.setBounds(23, 307, 121, 16);
			frame.getContentPane().add(lblFechaNacimiento);
			
			txtNombre = new JTextField();
			txtNombre.setBounds(353, 274, 130, 26);
			frame.getContentPane().add(txtNombre);
			txtNombre.setColumns(10);
			
			txtApellidos = new JTextField();
			txtApellidos.setBounds(654, 274, 130, 26);
			frame.getContentPane().add(txtApellidos);
			txtApellidos.setColumns(10);
			
			txtFechaNac = new JTextField();
			txtFechaNac.setBounds(156, 302, 130, 26);
			frame.getContentPane().add(txtFechaNac);
			txtFechaNac.setColumns(10);
			
			txtFoto = new JTextField();
			txtFoto.setBounds(654, 302, 130, 26);
			txtFoto.setEditable(false);
			txtFoto.setColumns(10);
			frame.getContentPane().add(txtFoto);
			
			JLabel lblFoto = new JLabel("");
			lblFoto.setBounds(723, 38, 183, 161);
			frame.getContentPane().add(lblFoto);
			
			JButton btnFoto = new JButton("Selecciona una foto");
			btnFoto.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					
					JFileChooser fileChooser = new JFileChooser();
			        
			        // Mostrar el cuadro de diálogo para seleccionar un archivo
			        int result = fileChooser.showOpenDialog(frame);
			        
			        // Si el usuario selecciona un archivo
			        if (result == JFileChooser.APPROVE_OPTION) {
			            // Obtener el archivo seleccionado
			            File selectedFile = fileChooser.getSelectedFile();
			            
			            // Crear un ImageIcon con el archivo seleccionado
			            ImageIcon imageIcon = new ImageIcon(selectedFile.getAbsolutePath());
			            
			            // Escalar la imagen para que quepa en el JLabel
			            Image image = imageIcon.getImage();
			            Image newImage = image.getScaledInstance(lblFoto.getWidth(), lblFoto.getHeight(), Image.SCALE_SMOOTH);
			            
			            // Actualizar el ImageIcon con la imagen escalada
			            imageIcon = new ImageIcon(newImage);
			            
			            // Mostrar la imagen en el JLabel
			            lblFoto.setIcon(imageIcon);
			        }
				}
			});
			btnFoto.setBounds(495, 302, 156, 29);
			frame.getContentPane().add(btnFoto);

			
			JButton btnBorrar = new JButton("Borrar");
			btnBorrar.setBounds(420, 353, 117, 29);
			btnBorrar.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (txtId.getText() == null || txtNombre.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Falta el nombre", "ERROR", JOptionPane.ERROR_MESSAGE);

					}else{
						
						try (Connection con = ConnectionSingleton.getConnection()) {
						     PreparedStatement dele_pstmt = con.prepareStatement("DELETE FROM persona_hobbie WHERE persona_id = ?");
						     dele_pstmt.setInt(1, Integer.valueOf(txtId.getText()));
						     int rowsDeleted =dele_pstmt.executeUpdate();
						     dele_pstmt.close();
						    
						   
						} catch (SQLException e2) {
						    e2.printStackTrace();
						}

							try {
								Connection con = ConnectionSingleton.getConnection();
								PreparedStatement ins_pstmt = con.prepareStatement("delete from personas where id=?");
								ins_pstmt.setInt(1, Integer.valueOf(txtId.getText()));
								ins_pstmt.executeUpdate();
								JOptionPane.showMessageDialog(null, "Persona borrada correctamente");

								mostrarPersona(modelUsuario);

							} catch (SQLException e1) {
								e1.printStackTrace();
								
							}
					}
				}
			});
			frame.getContentPane().add(btnBorrar);
			
			JButton btnAct = new JButton("Actualizar");
			btnAct.setBounds(589, 353, 117, 29);
			btnAct.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (txtNombre.getText() == null || txtNombre.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Falta el nombre", "ERROR", JOptionPane.ERROR_MESSAGE);

					} else if (txtApellidos.getText() == null || txtApellidos.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Faltan los apellidos", "ERROR", JOptionPane.ERROR_MESSAGE);

					} else if (txtFechaNac.getText() == null || txtFechaNac.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Falta la fecha nacimiento", "ERROR", JOptionPane.ERROR_MESSAGE);

					//} else if (txtFoto.getText() == null || txtFoto.getText().isEmpty()) {
						//JOptionPane.showMessageDialog(null, "Falta la foto", "ERROR", JOptionPane.ERROR_MESSAGE);

					} else {
						
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						
						LocalDate fechaInicio = LocalDate.parse(txtFechaNac.getText(), formatter);
						
						Period periodo = Period.between(fechaInicio, LocalDate.now());

						int edad = periodo.getYears();
						
						try {
							Connection con = ConnectionSingleton.getConnection();
							PreparedStatement ins_pstmt = con.prepareStatement(
									"update personas set nombre=?, apellidos=?,fecha_nacimiento=?,edad=?,foto=? where id=?");
							ins_pstmt.setString(1, txtNombre.getText()); // Primer “?”
							ins_pstmt.setString(2, txtApellidos.getText()); // Segundo “?”
							ins_pstmt.setString(3, txtFechaNac.getText()); // Tercer “?”
							ins_pstmt.setInt(4,edad );// cuarto “?”
							ins_pstmt.setString(5, txtFoto.getText()); // quinto “?”
							ins_pstmt.setInt(6, Integer.valueOf(txtId.getText())); // sexto “?”
							ins_pstmt.executeUpdate();
							
							mostrarPersona(modelUsuario);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						
						try (Connection con = ConnectionSingleton.getConnection()) {
						     PreparedStatement dele_pstmt = con.prepareStatement("DELETE FROM persona_hobbie WHERE persona_id = ?");
						     dele_pstmt.setInt(1, Integer.valueOf(txtId.getText()));
						     dele_pstmt.close();
						    
						   
						} catch (SQLException e2) {
						    e2.printStackTrace();
						}
						
						try (Connection con = ConnectionSingleton.getConnection()) {
						    PreparedStatement ins_pstmt = con.prepareStatement("INSERT INTO persona_hobbie (persona_id, hobbie) VALUES (?, ?)");

						    for (JCheckBox checkbox : checkboxesHobbies) {
						        if (checkbox.isSelected()) {
						            String hobbyNombre = checkbox.getText();
						            						            
						            ins_pstmt.setInt(1, Integer.valueOf(txtId.getText())); 
						            ins_pstmt.setString(2, hobbyNombre); 
						          
						            ins_pstmt.executeUpdate();
						        }
						    }
						    
						    JOptionPane.showMessageDialog(null, "Usuario actualizado");
						} catch (SQLException e2) {
						    e2.printStackTrace();
						}
						
						
						mostrarPersona(modelUsuario);
						
						
					}
					
				}
			});
			frame.getContentPane().add(btnAct);
			
			JButton btnCrear = new JButton("Crear");
			btnCrear.setBounds(243, 353, 117, 29);
			btnCrear.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					
					if (txtNombre.getText() == null || txtNombre.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Falta el nombre", "ERROR", JOptionPane.ERROR_MESSAGE);

					} else if (txtApellidos.getText() == null || txtApellidos.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Faltan los apellidos", "ERROR", JOptionPane.ERROR_MESSAGE);

					} else if (txtFechaNac.getText() == null || txtFechaNac.getText().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Falta la fecha nacimiento", "ERROR", JOptionPane.ERROR_MESSAGE);

					//} else if (txtFoto.getText() == null || txtFoto.getText().isEmpty()) {
						//JOptionPane.showMessageDialog(null, "Falta la foto", "ERROR", JOptionPane.ERROR_MESSAGE);

					} else {
						
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

						LocalDate fechaInicio = LocalDate.parse(txtFechaNac.getText(), formatter);

						Period periodo = Period.between(fechaInicio, LocalDate.now());

						int edad = periodo.getYears();
						
						try {
							Connection con = ConnectionSingleton.getConnection();
							PreparedStatement ins_pstmt = con.prepareStatement(
									"insert into personas (nombre, apellidos,fecha_nacimiento,edad,foto) VALUES (?,?,?,?,?)");
							ins_pstmt.setString(1, txtNombre.getText()); // Primer “?”
							ins_pstmt.setString(2, txtApellidos.getText()); // Segundo “?”
							ins_pstmt.setString(3, txtFechaNac.getText()); // Tercer “?”
							ins_pstmt.setInt(4,edad );// cuarto “?”
							ins_pstmt.setString(5, txtFoto.getText()); // quinto “?”
							ins_pstmt.executeUpdate();
							
							mostrarPersona(modelUsuario);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						int idUsu=0;
						try {
							Connection con = ConnectionSingleton.getConnection();
							PreparedStatement sel_pstmt = con.prepareStatement("SELECT id FROM personas WHERE nombre = ?");
							sel_pstmt.setString(1, txtNombre.getText());

							ResultSet rs_sel = sel_pstmt.executeQuery();

							while (rs_sel.next()) {
								idUsu = rs_sel.getInt("id");
								

							}

							rs_sel.close();
							sel_pstmt.close();

						} catch (SQLException e1) {
							e1.printStackTrace();

						}
						
						try (Connection con = ConnectionSingleton.getConnection()) {
						    PreparedStatement ins_pstmt = con.prepareStatement("INSERT INTO persona_hobbie (persona_id, hobbie) VALUES (?, ?)");

						    for (JCheckBox checkbox : checkboxesHobbies) {
						        if (checkbox.isSelected()) {
						            String hobbyNombre = checkbox.getText();

						            ins_pstmt.setInt(1, idUsu);
						            ins_pstmt.setString(2, hobbyNombre);
						          						           
						            ins_pstmt.executeUpdate();
						        }
						    }
						    
						    JOptionPane.showMessageDialog(null, "Usuario añadido");
						} catch (SQLException e2) {
						    e2.printStackTrace();
						}
						
					}
				}
			});
			frame.getContentPane().add(btnCrear);
			
			
			
			
			
			txtId = new JTextField();
			txtId.setBounds(137, 274, 130, 26);
			txtId.setEditable(false);
			frame.getContentPane().add(txtId);
			txtId.setColumns(10);
			
			JLabel lblId = new JLabel("Id:");
			lblId.setBounds(80, 279, 43, 16);
			frame.getContentPane().add(lblId);
			
			txtEdad = new JTextField();
			txtEdad.setBounds(353, 302, 130, 26);
			txtEdad.setEditable(false);
			frame.getContentPane().add(txtEdad);
			txtEdad.setColumns(10);
			
			JLabel lblEdad = new JLabel("Edad:");
			lblEdad.setBounds(298, 307, 43, 16);
			frame.getContentPane().add(lblEdad);
			
			

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 923, 712);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
