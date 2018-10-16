const fs = require('fs-extra')

fs.removeSync('../public/react'); 
fs.rename('./build', '../public/react', (err) => {
    if (err) throw err;
    console.log('Rename complete!');
});