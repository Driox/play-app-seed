import React, { Component } from 'react';
import 'App.css';
import { BrowserRouter as Router } from "react-router-dom";
import { Route, Link } from "react-router-dom";
import Profile from "pages/Profile";
import RunningJobs from "pages/RunningJobs";
import Dashboard from "pages/Dashboard";
import ErrorDisplay from "components/ErrorDisplay";
import { connect } from 'react-redux'

class MainTemplate extends Component {

  render() {
    return (
      <Router>
      <div className="App">
        <header className="App-header">
          <i className="material-icons md-48 App-logo">group_work</i>
          <h1 className="App-title">Welcome to React</h1>
        </header>
        <nav>
          <Link to="/app"><i className="material-icons md-18">home</i></Link>
          <Link to="/app/jobs">Running jobs</Link>
          <Link to="/app/user">Profile</Link>
        </nav>
        <div>
          <Route exact path="/app/jobs" component={RunningJobs}/>
          <Route exact path="/app/user" component={Profile}/>
          <Route exact path="/app" component={Dashboard} />
        </div>
        <ErrorDisplay errors={this.props.errors}/>
      </div>
      </Router>
    );
  }
}

export default connect( 
  (s => s)
)(MainTemplate);
