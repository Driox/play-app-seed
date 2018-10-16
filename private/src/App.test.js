import React from 'react';
import ReactDOM from 'react-dom';
import App from 'App';

// test data
window.SERVER_DATA = {
  user : { 
    email : "jean@gmail.com", 
    id : "1234" 
  }
};

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<App />, div);
  ReactDOM.unmountComponentAtNode(div);
});
