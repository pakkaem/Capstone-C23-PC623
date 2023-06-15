const mysql = require("mysql");
const db = mysql.createConnection({
  multipleStatements: true,
  host: "34.101.191.61",
  user: "root",
  password: "12345",
  database: "bllboarddb",
});

db.connect(function (err) {
  if (err) {
    console.log("error");
  } else {
    console.log("Database connected!");
  }
});

module.exports = db;