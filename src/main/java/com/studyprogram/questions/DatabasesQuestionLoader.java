package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class DatabasesQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.DATABASES; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("db-mc-01", Topic.DATABASES, 3,
                "What does JDBC stand for and what does it do?",
                "Java Data Bridge Connection — creates copies of databases",
                "Java Database Connectivity — a standard API for connecting Java programs to relational databases",
                "Java Default Binary Cache — stores query results",
                "Java Document Builder Core — parses XML database configs",
                "b",
                "JDBC (java.sql) provides interfaces: DriverManager, Connection, Statement, PreparedStatement, ResultSet. "
                + "It abstracts over different database vendors — the same code works with MySQL, PostgreSQL, SQLite, etc., "
                + "just swap the JDBC driver JAR and connection URL."),

            mc("db-mc-02", Topic.DATABASES, 3,
                "What is the advantage of PreparedStatement over Statement?",
                "PreparedStatement is faster to type",
                "PreparedStatement prevents SQL injection and can be reused with different parameters",
                "Statement supports more SQL keywords",
                "PreparedStatement auto-commits transactions",
                "b",
                "PreparedStatement pre-compiles the SQL and uses placeholders (?). "
                + "User input is passed as parameters (setString, setInt), never concatenated into the SQL — "
                + "this prevents SQL injection. The compiled plan can also be reused efficiently."),

            mc("db-mc-03", Topic.DATABASES, 3,
                "How do you iterate over the results of a SELECT query with JDBC?",
                "ResultSet extends Iterable, so use a for-each loop",
                "Call ResultSet.next() in a while loop — it advances the cursor and returns false when exhausted",
                "Call ResultSet.getAll() to get a List",
                "Query results are returned as an array of rows",
                "b",
                "while (rs.next()) { String name = rs.getString(\"name\"); int age = rs.getInt(\"age\"); } "
                + "rs.next() advances to the next row and returns true, or false if there are no more rows. "
                + "Columns are accessed by name or 1-based index."),

            mc("db-mc-04", Topic.DATABASES, 4,
                "What does connection.setAutoCommit(false) enable?",
                "Faster queries by skipping disk writes",
                "Manual transaction control — changes are not saved until connection.commit() is called",
                "Read-only mode for the connection",
                "Automatic retry on connection failure",
                "b",
                "By default, each SQL statement is committed immediately (autoCommit=true). "
                + "Disabling it lets you group multiple statements into one atomic transaction. "
                + "If any statement fails, call connection.rollback() to undo all changes in the transaction. "
                + "Always use try/catch to rollback on exception."),

            trace("db-tr-01", Topic.DATABASES, 3,
                "How many '?' placeholders are in this PreparedStatement?",
                "PreparedStatement ps = conn.prepareStatement(\n"
                + "    \"INSERT INTO students (name, grade, year) VALUES (?, ?, ?)\"\n"
                + ");",
                "3",
                "Three ? placeholders: one for name, one for grade, one for year. "
                + "Set them with ps.setString(1, name), ps.setInt(2, grade), ps.setInt(3, year) "
                + "(JDBC uses 1-based parameter indices)."),

            debug("db-db-01", Topic.DATABASES, 3,
                "This code is vulnerable to SQL injection. Why?",
                "String user = request.getParameter(\"username\");\n"
                + "Statement stmt = conn.createStatement();\n"
                + "ResultSet rs = stmt.executeQuery(\n"
                + "    \"SELECT * FROM users WHERE name = '\" + user + \"'\"\n"
                + ");",
                "createStatement() is deprecated",
                "User input is concatenated directly into the SQL string — a malicious user can inject arbitrary SQL",
                "executeQuery() returns the wrong type",
                "The WHERE clause syntax is invalid",
                "b",
                "If user = \"' OR '1'='1\", the query becomes: SELECT * FROM users WHERE name='' OR '1'='1' "
                + "— returning all rows. "
                + "Fix: use PreparedStatement with ? placeholder: "
                + "ps.setString(1, user) — the driver escapes special characters."),

            codegen("db-cg-01", Topic.DATABASES, 3,
                "Which correctly queries for a student by name using a PreparedStatement?",
                "Statement s = conn.createStatement(\"SELECT * FROM students WHERE name=?\"); s.setString(1, name);",
                "PreparedStatement ps = conn.prepareStatement(\"SELECT * FROM students WHERE name=?\"); ps.setString(1, name); ResultSet rs = ps.executeQuery();",
                "conn.query(\"SELECT * FROM students WHERE name=\" + name);",
                "PreparedStatement ps = new PreparedStatement(\"SELECT * FROM students WHERE name=?\", conn);",
                "b",
                "conn.prepareStatement(sql) compiles the SQL. setString(1, name) binds the parameter. "
                + "executeQuery() executes it and returns a ResultSet. "
                + "Option A: createStatement() takes no arguments. "
                + "Option D: PreparedStatement has no public constructor — get it from conn.prepareStatement().",
                "Use executeQuery() for SELECT; executeUpdate() for INSERT/UPDATE/DELETE."),

            mc("db-mc-05", Topic.DATABASES, 3,
                "What does ResultSet.getString(\"column\") do?",
                "Returns the column's value as a Java String for the current row",
                "Returns all values in the named column as a List<String>",
                "Sets the current row's column value to the given string",
                "Returns the SQL column definition",
                "a",
                "After rs.next(), getString(\"column\") retrieves the current row's value in the named column as a String. "
                + "For other types: getInt(\"age\"), getDouble(\"price\"), getBoolean(\"active\"). "
                + "Columns can also be accessed by 1-based index: getString(1)."),

            mc("db-mc-06", Topic.DATABASES, 3,
                "What happens if you forget to close a Connection in JDBC?",
                "Nothing — the JVM closes all connections on exit",
                "The connection stays open, consuming resources; under load this can exhaust the database's connection pool",
                "The next database call automatically reuses it",
                "The database server closes it after one query",
                "b",
                "Database connections are expensive resources. Leaking them (not closing) causes the pool to exhaust under load, "
                + "resulting in timeouts or errors for new requests. "
                + "Use try-with-resources: try (Connection conn = DriverManager.getConnection(...)) { ... }"),

            mc("db-mc-07", Topic.DATABASES, 4,
                "What is the role of a JDBC driver?",
                "A compiled SQL schema for the target database",
                "A library that implements the JDBC API for a specific database (e.g., MySQL Connector/J for MySQL)",
                "The JVM component that executes SQL",
                "A Java class that generates SQL from Java objects",
                "b",
                "The JDBC API (java.sql.*) defines interfaces. The driver implements them for a specific database. "
                + "You add the driver JAR to the classpath and load it via DriverManager.getConnection(url, user, pass). "
                + "Drivers follow the URL format: jdbc:mysql://host:port/db, jdbc:postgresql://..., etc."),

            trace("db-tr-02", Topic.DATABASES, 3,
                "How many columns does this ResultSet have?",
                "ResultSet rs = stmt.executeQuery(\"SELECT name, age, grade FROM students WHERE id=1\");\n"
                + "// rs.next() returns true (one row found)\n"
                + "ResultSetMetaData meta = rs.getMetaData();\n"
                + "System.out.println(meta.getColumnCount());",
                "3",
                "The SELECT lists 3 columns: name, age, grade. getColumnCount() returns 3. "
                + "ResultSetMetaData also provides getColumnName(i) and getColumnType(i) for each column."),

            trace("db-tr-03", Topic.DATABASES, 3,
                "What is printed by this code (assuming the row exists)?",
                "PreparedStatement ps = conn.prepareStatement(\"SELECT grade FROM students WHERE name=?\");\n"
                + "ps.setString(1, \"Alice\");\n"
                + "ResultSet rs = ps.executeQuery();\n"
                + "if (rs.next()) System.out.println(rs.getInt(\"grade\"));",
                "Alice's grade as an integer",
                "setString(1, \"Alice\") binds the parameter. executeQuery() runs the query. "
                + "rs.next() advances to the first row. getInt(\"grade\") retrieves Alice's numeric grade."),

            debug("db-db-02", Topic.DATABASES, 4,
                "The transaction changes are not saved even after commit(). Why?",
                "Connection conn = DriverManager.getConnection(url, user, pass);\n"
                + "// autoCommit is true (default)\n"
                + "conn.setAutoCommit(false);\n"
                + "stmt.executeUpdate(\"UPDATE accounts SET balance=balance-100 WHERE id=1\");\n"
                + "stmt.executeUpdate(\"UPDATE accounts SET balance=balance+100 WHERE id=2\");\n"
                + "// forgot to call conn.commit()\n"
                + "conn.close();",
                "setAutoCommit(false) prevents any saves permanently",
                "close() automatically commits open transactions",
                "Without conn.commit(), the transaction is rolled back when the connection closes",
                "executeUpdate() commits immediately regardless of autoCommit setting",
                "c",
                "When autoCommit is false, uncommitted changes are rolled back when the connection closes. "
                + "You must explicitly call conn.commit() to persist changes. "
                + "Always use: try { ...; conn.commit(); } catch (Exception e) { conn.rollback(); }"),

            codegen("db-cg-02", Topic.DATABASES, 4,
                "Which correctly inserts a row and handles the transaction safely?",
                "stmt.executeUpdate(\"INSERT INTO log VALUES (\" + msg + \")\");",
                "try (Connection conn = DriverManager.getConnection(url,u,p)) { conn.setAutoCommit(false); PreparedStatement ps = conn.prepareStatement(\"INSERT INTO log (msg) VALUES (?)\"); ps.setString(1, msg); ps.executeUpdate(); conn.commit(); } catch (SQLException e) { conn.rollback(); }",
                "conn.execute(\"INSERT INTO log VALUES (?)\", msg);",
                "PreparedStatement.insert(conn, \"log\", msg);",
                "b",
                "Use try-with-resources, disable autoCommit, prepare a statement with ? placeholder, execute, commit. "
                + "Catch SQLException and rollback to maintain consistency. "
                + "Option A is vulnerable to SQL injection (string concatenation).")
        );
    }
}
