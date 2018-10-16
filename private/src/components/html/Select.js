import React from 'react'

const Select = ({label, options, current_value, on_change}) => {
    return (
        <div>
            <span>{ label }</span>
            <select value = {current_value} onChange = { on_change }>
                {options.map( (elem, i) => 
                    <option key={elem.value} value={ elem.value }> {elem.label} </option>    
                )}
            </select>
        </div>
    );
}

export default Select;
