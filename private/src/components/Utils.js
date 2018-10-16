import T from 'i18n-react'
import * as globalActions from 'actions/GlobalActions'
import { connect } from 'react-redux'

function m(s){
    return T.translate(s);
}

function globalConnect(component){
    return connect( 
        (state, ownProps) => { return state; },
        { ...globalActions }
      )(component)
}

export { m, globalConnect };