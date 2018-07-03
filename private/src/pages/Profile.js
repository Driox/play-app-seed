import React, { Component } from 'react'
import { m, globalConnect } from 'components/Utils'
import Select from 'components/html/Select'

class Profile extends Component {

  available_lng() {
    let prefix = "global.lng.";
    let lngs = ["fr", "en"];
    return lngs.map(e => { 
      return {
        label : m(prefix + e), 
        value : e 
      }
    });
  }

  render() {
    const { user, lang } = this.props;

    return (
      <div className="Profile">
        <h1>user logged :  { user.email } </h1>
        <div>
          <Select 
            label= { m("profile.lng") }
            options = { this.available_lng() }
            current_value = { lang }
            on_change = { (e) => this.props.updateFavoredLanguage(user.id, e.target.value) } 
          />
        </div>
        <p className="App-intro">
          todo : profil user
        </p>
      </div>
    )
  }
}

export default globalConnect(Profile);
