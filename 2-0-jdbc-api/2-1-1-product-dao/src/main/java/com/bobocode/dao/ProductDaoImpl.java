package com.bobocode.dao;

import com.bobocode.exception.DaoOperationException;
import com.bobocode.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

public class ProductDaoImpl implements ProductDao {

    private final DataSource dataSource;

    public ProductDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Product product) {
        Objects.requireNonNull(product);
        String insertSQL = "INSERT INTO products (name, producer, price, expiration_date) VALUES (?, ?, ?, ?);";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            fillStatement(insertStatement, product);
            insertStatement.executeUpdate();
            product.setId(getIdFromDB(insertStatement));
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error saving product: %s", product), e);
        }
    }

    private void fillStatement(PreparedStatement emptyStatement, Product product) {
        try {
            emptyStatement.setString(1, product.getName());
            emptyStatement.setString(2, product.getProducer());
            emptyStatement.setBigDecimal(3, product.getPrice());
            emptyStatement.setDate(4, Date.valueOf(product.getExpirationDate()));
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error filling statement for save product: %s", product), e);
        }
    }

    private Long getIdFromDB(PreparedStatement executedStatement) throws SQLException {
        ResultSet rid = executedStatement.getGeneratedKeys();
        if (rid.next()) return rid.getLong(1);
        else throw new DaoOperationException("Error in attempt to retrieve id for new product");
    }

    @Override
    public List<Product> findAll() {
        String getAllSQL = "SELECT * FROM products;";
        List<Product> result = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement findAllStatement = connection.prepareStatement(getAllSQL);
            ResultSet resultSet = findAllStatement.executeQuery();
            while (resultSet.next()) {
                result.add(this.productMapper(resultSet));
            }
        } catch (SQLException e) {
            throw new DaoOperationException("Error finding all products", e);
        }
        return result;
    }

    @Override
    public Product findOne(Long id) {
        Objects.requireNonNull(id);
        String findByIdSQL = "SELECT * FROM products WHERE id = ?;";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement findById = connection.prepareStatement(findByIdSQL);
            findById.setLong(1, id);
            ResultSet resultSet = findById.executeQuery();

            if (resultSet.next()) {
                return productMapper(resultSet);

            } else throw new DaoOperationException(String.format("Product with id = %d does not exist", id));
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error finding product by id = %d", id), e);
        }
    }

    @Override
    public void update(Product product) {
        Objects.requireNonNull(product);
        checkIdIsNotNull(product);
        String updateSQL = "UPDATE products SET name = ?, producer = ?, price = ?, expiration_date = ? WHERE id = ?;";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement updateByIdStatement = connection.prepareStatement(updateSQL);
            fillStatement(updateByIdStatement, product);
            updateByIdStatement.setLong(5, product.getId());
            updateByIdStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error in attempt to update product with id: %d ", product.getId()), e);
        }
    }

    @Override
    public void remove(Product product) {
        Objects.requireNonNull(product);
        checkIdIsNotNull(product);
        String deleteSQL = "DELETE from products WHERE id = ?;";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
            deleteStatement.setLong(1, product.getId());
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoOperationException(String.format("Error in attempt to delete product with id: %d", product.getId()), e);
        }
    }

    private Product productMapper(ResultSet resultSet) {
        Product product = new Product();
        try {
            product.setId(resultSet.getLong("id"));
            product.setName(resultSet.getString("name"));
            product.setProducer(resultSet.getString("producer"));
            product.setPrice(resultSet.getBigDecimal("price"));
            product.setExpirationDate(resultSet.getDate("expiration_date").toLocalDate());
            product.setCreationTime(resultSet.getTimestamp("creation_time").toLocalDateTime());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return product;
    }

    private void checkIdIsNotNull(Product product) {
        if (product.getId() == null) {
            throw new DaoOperationException("Product id cannot be null");
        }
    }
}
