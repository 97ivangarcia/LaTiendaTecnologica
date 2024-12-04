package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONObject;

public class TiendaApp extends JFrame {
    private JComboBox<String> clientesBox;
    private JComboBox<String> categoriasBox;
    private JComboBox<String> productosBox;
    private JTextArea usuarioArea;
    private JTextArea historialArea;
    private JLabel stockValue;
    private JSpinner cantidadSpinner;
    private JPanel imagenesPanel;
    private JSONObject tiendaJSON;

    public TiendaApp() {
        setTitle("Tienda Virtual - Mi Empresa");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30, 30, 30));

        inicializarComponentes();
        crearTablaSQLite();
        cargarDatos();
    }

    private void inicializarComponentes() {
        // Banner
        JLabel banner = new JLabel();
        URL bannerUrl = getClass().getClassLoader().getResource("bannertienda.png");

        if (bannerUrl != null) {
            banner.setIcon(new ImageIcon(bannerUrl));
        } else {
            banner.setText("No se pudo cargar el banner");
            banner.setHorizontalAlignment(SwingConstants.CENTER);
            banner.setForeground(new Color(200, 200, 200));
        }
        banner.setHorizontalAlignment(SwingConstants.CENTER);
        banner.setPreferredSize(new Dimension(1200, 200));
        banner.setBorder(new EmptyBorder(10, 10, 10, 10));
        banner.setOpaque(true);
        banner.setBackground(new Color(40, 40, 40));

        // Panel Usuario
        JPanel panelUsuario = crearPanelConTitulo("Datos del Usuario");
        panelUsuario.setLayout(new BorderLayout());
        clientesBox = new JComboBox<>();
        clientesBox.setBackground(new Color(50, 50, 50));
        clientesBox.setForeground(new Color(200, 200, 200));
        clientesBox.setFont(new Font("Open Sans", Font.PLAIN, 18)); // Tamaño aumentado
        clientesBox.setBorder(new LineBorder(new Color(128, 0, 255), 1, true));
        clientesBox.addActionListener(e -> actualizarCliente());
        usuarioArea = crearTextArea();
        panelUsuario.add(clientesBox, BorderLayout.NORTH);
        panelUsuario.add(new JScrollPane(usuarioArea), BorderLayout.CENTER);

        // Panel Productos
        JPanel panelProductos = crearPanelConTitulo("Seleccionar Categoría y Producto");
        categoriasBox = new JComboBox<>();
        categoriasBox.setBackground(new Color(50, 50, 50));
        categoriasBox.setForeground(new Color(200, 200, 200));
        categoriasBox.setFont(new Font("Open Sans", Font.PLAIN, 18)); // Tamaño aumentado
        categoriasBox.setBorder(new LineBorder(new Color(128, 0, 255), 1, true));
        categoriasBox.addActionListener(e -> cargarProductos());

        productosBox = new JComboBox<>();
        productosBox.setBackground(new Color(50, 50, 50));
        productosBox.setForeground(new Color(200, 200, 200));
        productosBox.setFont(new Font("Open Sans", Font.PLAIN, 18)); // Tamaño aumentado
        productosBox.setBorder(new LineBorder(new Color(128, 0, 255), 1, true));
        productosBox.addActionListener(e -> mostrarProductoSeleccionado());

        stockValue = new JLabel("20");
        stockValue.setFont(new Font("Roboto", Font.BOLD, 22)); // Tamaño aumentado
        stockValue.setForeground(new Color(128, 0, 255));

        cantidadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        cantidadSpinner.setPreferredSize(new Dimension(100, 40)); // Tamaño aumentado
        cantidadSpinner.setFont(new Font("Open Sans", Font.PLAIN, 18)); // Tamaño aumentado
        cantidadSpinner.setBackground(new Color(50, 50, 50));
        cantidadSpinner.setForeground(new Color(200, 200, 200));

        JPanel seleccionPanel = new JPanel(new FlowLayout());
        seleccionPanel.setBackground(new Color(40, 40, 40));
        seleccionPanel.add(categoriasBox);
        seleccionPanel.add(productosBox);
        seleccionPanel.add(new JLabel("Stock:"));
        seleccionPanel.add(stockValue);
        seleccionPanel.add(new JLabel("Cantidad:"));
        seleccionPanel.add(cantidadSpinner);

        imagenesPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagenesPanel.setBackground(new Color(40, 40, 40));
        panelProductos.add(seleccionPanel, BorderLayout.NORTH);
        panelProductos.add(imagenesPanel, BorderLayout.CENTER);

        // Panel Historial
        JPanel panelHistorial = crearPanelConTitulo("Historial de Compras");
        historialArea = crearTextArea();
        panelHistorial.add(new JScrollPane(historialArea), BorderLayout.CENTER);

        // Botón Comprar
        JButton btnComprar = new JButton("Comprar");
        btnComprar.setFont(new Font("Open Sans", Font.BOLD, 18)); // Tamaño aumentado
        btnComprar.setBackground(new Color(128, 0, 255));
        btnComprar.setForeground(Color.WHITE);
        btnComprar.setFocusPainted(false);
        btnComprar.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30)); // Márgenes aumentados
        btnComprar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnComprar.addActionListener(e -> realizarCompra());

        // Agregar componentes al JFrame
        add(banner, BorderLayout.NORTH);
        add(panelUsuario, BorderLayout.WEST);
        add(panelProductos, BorderLayout.CENTER);
        add(panelHistorial, BorderLayout.EAST);
        add(btnComprar, BorderLayout.SOUTH);
    }

    private JPanel crearPanelConTitulo(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(128, 0, 255), 1, true),
                titulo, 0, 0, new Font("Open Sans", Font.BOLD, 16), Color.WHITE));
        panel.setBackground(new Color(50, 50, 50));
        return panel;
    }

    private JTextArea crearTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(new Color(40, 40, 40));
        textArea.setForeground(Color.WHITE);
        textArea.setFont(new Font("Open Sans", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return textArea;
    }

    private ImageIcon crearImagenRedondeadaConBorde(Image originalImage, int width, int height, Color borderColor, int borderThickness) {
        int diameter = Math.min(width, height);
        BufferedImage roundedImage = new BufferedImage(width + borderThickness * 2, height + borderThickness * 2, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = roundedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo transparente
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, roundedImage.getWidth(), roundedImage.getHeight());
        g2.setComposite(AlphaComposite.SrcOver);

        // Dibujar borde
        g2.setColor(borderColor);
        g2.fillOval(0, 0, diameter + borderThickness * 2, diameter + borderThickness * 2);

        // Dibujar imagen redondeada
        g2.setClip(new java.awt.geom.Ellipse2D.Float(borderThickness, borderThickness, diameter, diameter));
        g2.drawImage(originalImage, borderThickness, borderThickness, width, height, null);
        g2.dispose();

        return new ImageIcon(roundedImage);
    }



    private void cargarDatos() {
        tiendaJSON = cargarArchivoJSON("DatosJSON.json");
        if (tiendaJSON != null) {
            JSONObject tienda = tiendaJSON.getJSONObject("tienda");
            cargarClientes(tienda.getJSONArray("usuarios"));
            cargarCategorias(tienda.getJSONArray("categorias"));
        }
    }

    private JSONObject cargarArchivoJSON(String rutaArchivo) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(rutaArchivo)) {
            if (inputStream == null) throw new IOException("Archivo no encontrado: " + rutaArchivo);
            return new JSONObject(new String(inputStream.readAllBytes()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar el JSON: " + e.getMessage());
            return null;
        }
    }

    private void guardarDatosJSON(String nombreArchivo) {
        try {
            // Obtiene la ruta del archivo en la carpeta resources
            File archivo = new File(Paths.get(getClass().getClassLoader().getResource(nombreArchivo).toURI()).toString());
            try (FileWriter writer = new FileWriter(archivo)) {
                writer.write(tiendaJSON.toString(4));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el JSON: " + e.getMessage());
        }
    }

    private void cargarClientes(JSONArray usuarios) {
        clientesBox.removeAllItems();
        for (int i = 0; i < usuarios.length(); i++) {
            clientesBox.addItem(usuarios.getJSONObject(i).getString("nombre"));
        }
        if (clientesBox.getItemCount() > 0) {
            clientesBox.setSelectedIndex(0);
            actualizarCliente();
        }
    }

    private void cargarCategorias(JSONArray categorias) {
        categoriasBox.removeAllItems();
        for (int i = 0; i < categorias.length(); i++) {
            categoriasBox.addItem(categorias.getJSONObject(i).getString("nombre"));
        }
    }

    private void actualizarCliente() {
        String clienteSeleccionado = (String) clientesBox.getSelectedItem();
        if (clienteSeleccionado != null) {
            JSONArray usuarios = tiendaJSON.getJSONObject("tienda").getJSONArray("usuarios");
            for (int i = 0; i < usuarios.length(); i++) {
                JSONObject usuario = usuarios.getJSONObject(i);
                if (usuario.getString("nombre").equals(clienteSeleccionado)) {
                    usuarioArea.setText("Nombre: " + usuario.getString("nombre") + "\nEmail: " + usuario.getString("email"));
                    actualizarHistorial(clienteSeleccionado);
                    break;
                }
            }
        }
    }

    private void cargarProductos() {
        productosBox.removeAllItems();
        String categoriaSeleccionada = (String) categoriasBox.getSelectedItem();
        if (categoriaSeleccionada != null) {
            JSONArray categorias = tiendaJSON.getJSONObject("tienda").getJSONArray("categorias");
            for (int i = 0; i < categorias.length(); i++) {
                JSONObject categoria = categorias.getJSONObject(i);
                if (categoria.getString("nombre").equals(categoriaSeleccionada)) {
                    JSONArray productos = categoria.getJSONArray("productos");
                    for (int j = 0; j < productos.length(); j++) {
                        productosBox.addItem(productos.getJSONObject(j).getString("nombre"));
                    }
                    break;
                }
            }
        }
    }

    private void mostrarProductoSeleccionado() {
        String categoriaSeleccionada = (String) categoriasBox.getSelectedItem();
        String productoSeleccionado = (String) productosBox.getSelectedItem();
        if (categoriaSeleccionada != null && productoSeleccionado != null) {
            JSONArray categorias = tiendaJSON.getJSONObject("tienda").getJSONArray("categorias");
            for (int i = 0; i < categorias.length(); i++) {
                JSONObject categoria = categorias.getJSONObject(i);
                if (categoria.getString("nombre").equals(categoriaSeleccionada)) {
                    JSONArray productos = categoria.getJSONArray("productos");
                    for (int j = 0; j < productos.length(); j++) {
                        JSONObject producto = productos.getJSONObject(j);
                        if (producto.getString("nombre").equals(productoSeleccionado)) {
                            int inventario = producto.getInt("inventario");
                            stockValue.setText(String.valueOf(inventario));
                            cantidadSpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(1, inventario), 1));
                            mostrarImagenesProducto(producto.getJSONArray("imagenes"));
                            break;
                        }
                    }
                }
            }
        }
    }

    private void mostrarImagenesProducto(JSONArray imagenes) {
        imagenesPanel.removeAll();
        for (int i = 0; i < imagenes.length(); i++) {
            String nombreImagen = imagenes.getString(i);
            URL imagenUrl = getClass().getClassLoader().getResource(nombreImagen);

            if (imagenUrl != null) {
                // Cargar la imagen original y aplicar borde y redondeado
                ImageIcon originalIcon = new ImageIcon(imagenUrl);
                ImageIcon roundedIcon = crearImagenRedondeadaConBorde(
                        originalIcon.getImage(),
                        400, 400, // Tamaño de la imagen redondeada
                        new Color(128, 0, 255), // Color del borde morado
                        10 // Grosor del borde
                );

                JLabel labelImagen = new JLabel(roundedIcon);
                labelImagen.setHorizontalAlignment(SwingConstants.CENTER);
                imagenesPanel.add(labelImagen);
            } else {
                JLabel labelError = new JLabel("Imagen no encontrada: " + nombreImagen);
                labelError.setForeground(Color.RED);
                labelError.setHorizontalAlignment(SwingConstants.CENTER);
                imagenesPanel.add(labelError);
            }
        }
        imagenesPanel.revalidate();
        imagenesPanel.repaint();
    }



    private void realizarCompra() {
        String cliente = (String) clientesBox.getSelectedItem();
        String producto = (String) productosBox.getSelectedItem();
        int cantidad = (int) cantidadSpinner.getValue();
        int stockDisponible = Integer.parseInt(stockValue.getText());

        if (cliente != null && producto != null) {
            if (cantidad <= stockDisponible) {
                try (Connection conn = conectarSQLite()) {
                    String sql = "INSERT INTO historial_compras (cliente, producto, cantidad, fecha) VALUES (?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, cliente);
                    pstmt.setString(2, producto);
                    pstmt.setInt(3, cantidad);
                    pstmt.setString(4, LocalDate.now().toString());
                    pstmt.executeUpdate();

                    stockDisponible -= cantidad;
                    actualizarInventarioJSON(producto, stockDisponible);
                    stockValue.setText(String.valueOf(stockDisponible));
                    cantidadSpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(1, stockDisponible), 1));
                    JOptionPane.showMessageDialog(this, "Compra registrada exitosamente.");
                    actualizarHistorial(cliente);

                    guardarDatosJSON("DatosJSON.json");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Error al registrar la compra: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "Stock insuficiente. Solo quedan " + stockDisponible + " unidades.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente y un producto antes de realizar la compra.");
        }
    }

    private void actualizarInventarioJSON(String productoNombre, int nuevoInventario) {
        JSONArray categorias = tiendaJSON.getJSONObject("tienda").getJSONArray("categorias");
        for (int i = 0; i < categorias.length(); i++) {
            JSONArray productos = categorias.getJSONObject(i).getJSONArray("productos");
            for (int j = 0; j < productos.length(); j++) {
                JSONObject producto = productos.getJSONObject(j);
                if (producto.getString("nombre").equals(productoNombre)) {
                    producto.put("inventario", nuevoInventario);
                    return;
                }
            }
        }
    }

    private void actualizarHistorial(String cliente) {
        historialArea.setText("");
        try (Connection conn = conectarSQLite()) {
            String sql = "SELECT * FROM historial_compras WHERE cliente = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, cliente);
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                String producto = rs.getString("producto");
                int cantidad = rs.getInt("cantidad");
                double precio = obtenerPrecioProducto(producto);

                historialArea.append("Producto: " + producto +
                        ", Cantidad: " + cantidad +
                        ", Precio: $" + (cantidad * precio) + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double obtenerPrecioProducto(String productoNombre) {
        JSONArray categorias = tiendaJSON.getJSONObject("tienda").getJSONArray("categorias");
        for (int i = 0; i < categorias.length(); i++) {
            JSONArray productos = categorias.getJSONObject(i).getJSONArray("productos");
            for (int j = 0; j < productos.length(); j++) {
                JSONObject producto = productos.getJSONObject(j);
                if (producto.getString("nombre").equals(productoNombre)) {
                    return producto.getDouble("precio");
                }
            }
        }
        return 0.0; // Devuelve 0 si no encuentra el producto
    }


    private Connection conectarSQLite() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:compras.db");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage());
            return null;
        }
    }

    private void crearTablaSQLite() {
        try (Connection conn = conectarSQLite()) {
            if (conn != null) {
                String sql = "CREATE TABLE IF NOT EXISTS historial_compras (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "cliente TEXT, " +
                        "producto TEXT, " +
                        "cantidad INTEGER, " +
                        "fecha TEXT)";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al crear la tabla: " + e.getMessage());
        }
    }

}
