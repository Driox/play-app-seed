import React from 'react'

const ErrorDisplay = ({errors}) => {
    if(errors.error_msg){
        return  (
            <div className="errors">
                <b>{errors.error_msg} </b>
                <a onClick={ (e) => document.location.reload(true) }><i className="material-icons md-24">refresh</i></a>
            </div>
        );
    } else {
        return "";
    }
}

export default ErrorDisplay;