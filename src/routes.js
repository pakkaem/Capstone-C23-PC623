const express = require("express");
const router = express.Router();
const db = require("./database");
const bcrypt = require("bcryptjs");
const logger = require("./logger");
const shortId = require("short-uuid");
const jwt = require("jsonwebtoken");
const cookie = require("cookie-parser");

// register
router.post("/register", (req, res) => {
  const { nama, email, password } = req.body;
  db.query(
    `SELECT * FROM users WHERE LOWER(email) = LOWER(${db.escape(email)});`,
    (err, result) => {
      if (result.length) {
        return res.status(409).send({
          msg: "This user is already in use!",
        });
      } else {
        bcrypt.hash(password, 10, (err, hash) => {
          if (err) {
            return res.status(500).send({
              msg: `error at ${err}`,
            });
          } else {
            const id = shortId.generate();
            db.query(
              `INSERT INTO users VALUES ("${id}", "${nama}", ${db.escape(
                email
              )}, ${db.escape(hash)})`,
              (err, result) => {
                if (err) {
                  throw err;
                  return res.status(400).send({
                    msg: err,
                  });
                }
                return res.status(201).send({
                  msg: "User Success Register!",
                });
              }
            );
          }
        });
      }
    }
  );
});

router.post("/login", (req, res) => {
  const { email, password } = req.body;
  db.query(
    `SELECT * FROM users WHERE email = ${db.escape(email)};`,
    (err, result) => {
      if (err) {
        throw err;
        return res.status(400).send({
          msg: err,
        });
      }
      if (!result.length) {
        return res.status(401).send({
          msg: "Email or password is incorrect!",
        });
      }
      bcrypt.compare(password, result[0]["password"], (bError, bResult) => {
        if (bError) {
          throw bError;
          return res.status(401).send({
            msg: "Email or password is incorrect!",
          });
        }
        if (bResult) {
          const id = result[0].id;
          const token = jwt.sign({ id }, "the-super-strong-secrect", {
            expiresIn: "1d",
          });
          res.cookie("token", token);
          logger.log("info", `Succees Login ${email}`);
          return res.status(200).send({
            token,
            user: result[0],
          });
        }
        return res.status(401).send({
          msg: "Username or password is incorrect!",
        });
      });
    }
  );
});

// Logout
router.get("/logout", (req, res) => {
  try {
    const token = req.cookies.token;
    if (!token) {
      return res.status(401).send({
        message: "need login or token null",
      });
    } else {
      res.clearCookie("token");
      return res.status(200).send({
        message: "Success Logout!",
      });
    }
  } catch (err) {
    return res.status(500).send({
      message: `${err}`,
    });
  }
});


module.exports = router;
