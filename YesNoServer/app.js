
const express    = require('express');
const bodyParser = require('body-parser');
const passport   = require('passport');
const BasicStrategy = require('passport-http').BasicStrategy;
const crypto     = require('crypto');
const storage    = require('./storage.js');

const app = express();
app.use(function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'PUT, GET, POST, DELETE, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');
    next();
});
app.use(bodyParser.urlencoded());
app.use(passport.initialize());
app.use(passport.session());

passport.use(new BasicStrategy(
  function(username, password, done) {
    var session = storage.persistenceStore.getSession();
    storage.entities.User.all(session).filter('username', '=', username).list(function(users) {
      session.close();
      
      var shasum = crypto.createHash('sha1');
      shasum.update(password);
      var hash = shasum.digest('hex');
      
      if (users.length > 0 && users[0].password == hash) {
        return done(null, users[0]);
      }
      return done("Access denied!");
    });
  }
));

require('./services.js')(app, storage, passport);

app.get('/', passport.authenticate('basic', { session: false }), function(req, res) {
  res.sendFile(__dirname + "/static/index.html");
});

app.use(express.static('static'));
app.use('/bootstrap', express.static(__dirname + '/node_modules/bootstrap/dist/'));
app.use('/angular-ui-bootstrap', express.static(__dirname + '/node_modules/angular-ui-bootstrap/dist/'));
app.use('/angular', express.static(__dirname + '/node_modules/angular/'));
app.use('/angular-resource', express.static(__dirname + '/node_modules/angular-resource/'));
app.use('/angular-ui-router', express.static(__dirname + '/node_modules/angular-ui-router/release/'));
app.use('/ng-file-upload', express.static(__dirname + '/node_modules/ng-file-upload/dist/'));

const listeningPort = process.env.OPENSHIFT_NODEJS_PORT || 3000;
const listeningAddress = process.env.OPENSHIFT_NODEJS_IP || '127.0.0.1'
console.log("Preparing for listening on port "+listeningPort+"...");
app.listen(listeningPort, listeningAddress, function() {
    console.log("Listening...");
});

