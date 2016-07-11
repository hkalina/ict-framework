const bodyParser  = require('body-parser');
const multer      = require('multer');
const sanitizeFn  = require('sanitize-filename');
const fs          = require('fs');

const filesDir = process.env.OPENSHIFT_DATA_DIR || __dirname + '/uploads';
const upload = multer({ dest: filesDir });

module.exports = function(app, storage, passport) {
  
  // MOBILE + WEB
  app.post('/cards/create', bodyParser.json(),
           passport.authenticate('basic', { session: false }),
           function(req, res) {
    console.log(req.body);
    if (req.user.username == 'demo') {
      res.send('Pristup odepren! Uzivatel demo muze jen cist!', 403);
      return;
    }
    
    var session = storage.persistenceStore.getSession();
    var card = new storage.entities.Card(session);
    
    session.add(card);
    console.log("name = " + req.body.name);
    card.name = req.body.name;
    card.text = req.body.text;
    card.color = req.body.color;
    card.image = null;
    card.sound = null;
    card.owner = req.user.id;
    session.flush();
    session.close();
    res.send(card.id);
  });

  // WEB
  app.post('/cards/update', bodyParser.json(),
           passport.authenticate('basic', { session: false }),
           function(req, res) {
    if (req.user.username == 'demo') {
      res.send('Pristup odepren! Uzivatel demo muze jen cist!', 403);
      return;
    }
    
    var session = storage.persistenceStore.getSession();
    storage.entities.Card.load(session, req.body.id, function(card) {
      if (card == null) {
        session.close();
        return res.status(404).send('Card '+req.body.id+' not exists');
      }
      card.fetch("owner", function(owner) {
        console.log("owner:");
        console.log(owner.id);
        
        if (!card.owner || card.owner.id != req.user.id) {
          session.close();
          return res.status(403).send('User is not owner!');
        }
        
        card.name = req.body.name;
        card.text = req.body.text;
        card.color = req.body.color;
        session.flush();
        session.close();
        res.send(card.id);
      });
    });
  });
  
  // MOBILE
  app.post('/cards/attach', bodyParser.json(),
           upload.single('file'),
           passport.authenticate('basic', { session: false }),
           function(req, res) {
    if (req.user.username == 'demo') {
      res.send('Pristup odepren! Uzivatel demo muze jen cist!', 403);
      return;
    }
    
    var session = storage.persistenceStore.getSession();
    storage.entities.Card.load(session, req.body.id, function(card) {
      if (card == null) {
        session.close();
        return res.status(404).send('Card '+req.body.id+' not exists');
      }
      card.fetch("owner", function(owner) {
        console.log("owner:");
        console.log(owner.id);
        
        if (!card.owner || card.owner.id != req.user.id) {
          session.close();
          return res.status(403).send('User is not owner!');
        }
        if (req.body.type == 'image') {
          if (card.image) fs.unlink(filesDir + "/" + sanitizeFn(card.image), function(e) { console.log(e); });
          card.image = req.file.filename;
        } else if (req.body.type == 'sound') {
          if (card.sound) fs.unlink(filesDir + "/" + sanitizeFn(card.sound), function(e) { console.log(e); });
          card.sound = req.file.filename;
        } else {
          session.close();
          return res.status(404).send('Unknown type '+req.body.type);
        }
        session.flush();
        session.close();
        res.send("OK");
      });
    });
  });
  
  // MOBILE
  app.get('/cards/list',
          passport.authenticate('basic', { session: false }),
          function(req, res) {
    var session = storage.persistenceStore.getSession();
    storage.entities.Card.all(session).order('name', true).list(function(cards) {
      session.close();
      res.setHeader('Content-Type', 'application/json');
      res.send(JSON.stringify(cards));
    });
  });
  
  // MOBILE
  app.get('/download/:id', function(req, res) {
    res.type('image/jpeg');
    res.sendFile(filesDir + "/" + sanitizeFn(req.params.id));
  });
  
  // WEB
  app.get('/cards/get/:id',
          passport.authenticate('basic', { session: false }),
          function(req, res) {
    var session = storage.persistenceStore.getSession();
    storage.entities.Card.load(session, req.params.id, function(card) {
      session.close();
      res.setHeader('Content-Type', 'application/json');
      res.send(JSON.stringify(card));
    });
  });
  
  // WEB
  app.delete('/cards/delete/:id',
          passport.authenticate('basic', { session: false }),
          function(req, res) {
    if (req.user.username == 'demo') {
      res.send('Pristup odepren! Uzivatel demo muze jen cist!', 403);
      return;
    }
    
    var session = storage.persistenceStore.getSession();
    storage.entities.Card.load(session, req.params.id, function(card) {
      if (card == null) {
        session.close();
        return res.status(404).send('Deleting non existing card');
      }
      card.fetch("owner", function(owner) {
        if (!card.owner || card.owner.id != req.user.id) {
          session.close();
          return res.status(403).send('User is not owner!');
        }
        
        if (card.image) fs.unlink(filesDir + "/" + sanitizeFn(card.image), function(e) { console.log(e); });
        if (card.sound) fs.unlink(filesDir + "/" + sanitizeFn(card.sound), function(e) { console.log(e); });
        storage.persistence.remove(card);
        
        session.transaction(function(tx) {
          storage.persistence.flush(tx, function() {
            tx.commit(session, function() {
              session.close();
              console.log("card removed");
              res.send('REMOVED');
            });
          });
        });
        
      });
    });
  });
  
  // MOBILE
  app.post('/lists/create', bodyParser.json(),
           passport.authenticate('basic', { session: false }),
           function(req, res) {
    if (req.user.username == 'demo') {
      res.send('Pristup odepren! Uzivatel demo muze jen cist!', 403);
      return;
    }
    
    var session = storage.persistenceStore.getSession();
    var list = new storage.entities.List(session);
    
    session.add(list);
    list.name = req.body.name;
    list.owner = req.user.id;
    session.flush();
    session.close();
    res.send(list.id);
  });
  
  // MOBILE
  app.post('/lists/addpair', bodyParser.json(),
           passport.authenticate('basic', { session: false }),
           function(req, res) {
    if (req.user.username == 'demo') {
      res.send('Pristup odepren! Uzivatel demo muze jen cist!', 403);
      return;
    }
    
    var session = storage.persistenceStore.getSession();
    storage.entities.List.load(session, req.body.id, function(list) {
      if (list == null) {
        session.close();
        return res.status(404).send('List '+req.body.id+' not exists');
      }
      list.fetch("owner", function(owner) {
        console.log("owner:");
        console.log(owner.id);
        
        if (!list.owner || list.owner.id != req.user.id) {
          session.close();
          return res.status(403).send('User is not owner!');
        }
        
        var pair = new storage.entities.Pair(session);
        
        if (req.body.leftId && req.body.rightId) {
          storage.entities.Card.load(session, req.body.leftId, function(leftCard) {
            storage.entities.Card.load(session, req.body.rightId, function(rightCard) {
              pair.left = leftCard;
              pair.right = rightCard;
              list.pairs.add(pair);
              session.flush();
              session.close();
              res.send("OK");
            });
          });
          return;
        }
        
        if (req.body.leftId) {
          storage.entities.Card.load(session, req.body.leftId, function(leftCard) {
              pair.left = leftCard;
              list.pairs.add(pair);
              session.flush();
              session.close();
              res.send("OK");
          });
          return;
        }
        
        if (req.body.rightId) {
          storage.entities.Card.load(session, req.body.rightId, function(rightCard) {
              pair.right = rightCard;
              list.pairs.add(pair);
              session.flush();
              session.close();
              res.send("OK");
          });
          return;
        }
        
        // without left and right
        list.pairs.add(pair);
        session.flush();
        session.close();
        res.send("OK");
      });
    });
  });
  
  // WEB
  app.delete('/lists/delete/:id',
          passport.authenticate('basic', { session: false }),
          function(req, res) {
    if (req.user.username == 'demo') {
      res.send('Pristup odepren! Uzivatel demo muze jen cist!', 403);
      return;
    }
    
    var session = storage.persistenceStore.getSession();
    storage.entities.List.load(session, req.params.id, function(list) {
      if (list == null) {
        session.close();
        return res.status(404).send('List '+req.params.id+' not exists');
      }
      list.fetch("owner", function(owner) {
        console.log("owner:");
        console.log(owner.id);
        
        if (!list.owner || list.owner.id != req.user.id) {
          session.close();
          return res.status(403).send('User is not owner!');
        }
        
        list.pairs.destroyAll();
        storage.persistence.remove(list);
        
        session.transaction(function(tx) {
          storage.persistence.flush(tx, function() {
            tx.commit(session, function() {
              session.close();
              console.log("card removed");
              res.send('REMOVED');
            });
          });
        });
      });
    });
  });
  
  // MOBILE
  app.get('/lists/list',
          passport.authenticate('basic', { session: false }),
          function(req, res) {
    var session = storage.persistenceStore.getSession();
    storage.entities.List.all(session).order('name', true).list(function(lists) {
      session.close();
      res.setHeader('Content-Type', 'application/json');
      res.send(JSON.stringify(lists));
    });
  });
  
  // MOBILE
  app.get('/lists/get/:id',
          passport.authenticate('basic', { session: false }),
          function(req, res) {
    var session = storage.persistenceStore.getSession();
    storage.entities.List.load(session, req.params.id, function(list) {
      if (list == null) {
        session.close();
        return res.status(404).send('List '+req.params.id+' not exists');
      }
      
      list.pairs.prefetch("left").prefetch("right").list(null, function(pairs) {
        
        var out = [];
        pairs.forEach(function(value) {
          out.push({
            left: value.left,
            right: value.right
          });
        });
        
        session.close();
        res.setHeader('Content-Type', 'application/json');
        res.send(JSON.stringify(out));
      });
    });
  });
  
  // OPENSHIFT
  app.get('/health',function(req, res) {
    var session = storage.persistenceStore.getSession();
    session.close();
    res.send('OK'); // OpenShift require code 200 for this URL
  });
  
};
