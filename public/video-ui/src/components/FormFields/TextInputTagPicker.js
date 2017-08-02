import React from 'react';
import { keyCodes } from '../../constants/keyCodes';
import UserActions from '../../constants/UserActions';
import TagTypes from '../../constants/TagTypes';
import CapiSearch from '../CapiSearch/Capisearch';
import removeStringTagDuplicates from '../../util/removeStringTagDuplicates';
import TagFieldValue from '../Tags/TagFieldValue';

export default class TextInputTagPicker extends React.Component {

  state = {
    inputString: '',
    lastAction: UserActions.other,
  };

  selectNewTag = (newFieldValue) => {

      this.setState({
        inputString: ''
      });

      this.props.onUpdate(newFieldValue);
  }

  getYoutubeInputValue = () => {
    if (
      this.props.tagValue.every(value => {
        return value.id !== this.state.inputString;
      })
    ) {
      return {
        id: this.state.inputString,
        webTitle: this.state.inputString
      };
    }
    return [];
  };

  updateInput = e => {
      // If the user did not add new text input, we update the tag search
    if (this.state.lastAction === UserActions.delete) {
      const length = this.props.tagValue.length;
      const lastInput = this.props.tagValue[length - 1];

      this.setState({
        inputString: lastInput,
        lastAction: UserActions.other
      });

      const newValue = this.props.tagValue.slice(
        0,
        this.props.tagValue.length - 1
      );
      this.props.onUpdate(newValue);
    } else {
      this.setState({
        inputString: e.target.value
      });

      if (!this.props.disableCapiTags) {
        const searchText = e.target.value;

        this.props.fetchTags(searchText);

      }
    }
  };

  processTagInput = e => {
    if (e.keyCode === keyCodes.enter) {
      const onlyWhitespace = !/\S/.test(this.state.inputString);
      if (!onlyWhitespace) {

        const newInput = this.props.tagType === TagTypes.youtube ? this.getYoutubeInputValue() : this.state.inputString;

        const newFieldValue = this.props.tagValue.concat([newInput]);

        this.props.onUpdate(newFieldValue);
        this.setState({
          inputString: '',
        });
      }

    } else if (e.keyCode === keyCodes.backspace) {
      if (this.state.inputString.length === 0) {
        const lastInput = this.props.tagValue[this.props.tagValue.length - 1];

        if (typeof lastInput === 'string') {
          //User is trying to delete a string input
          this.setState(
            {
              lastAction: UserActions.delete
            },
            () => {
              this.updateInput();
            }
          );
        }
      }
    } else {
      this.setState({
        lastAction: UserActions.other
      });
    }
  };

  renderValue = (field, i) => {
    const removeFn = () => {
      const newFieldValue = this.props.tagValue.filter(oldField => {
        return field.id !== oldField.id;
      });

      this.setState({
        inputString: ''
      });
      this.props.onUpdate(newFieldValue);
    };

    if (field.id) {
      return (
        <span
          className="form__field--multiselect__value form__field__tag__remove"
          key={`${field.id}-${i}`}
          onClick={removeFn}
        >
          {field.webTitle}{' '}
        </span>
      );
    }
    return (
      <span
        className="form__field--multistring__value"
        key={`${field.id}-${i}`}
      >
        {' '}{field}{' '}
      </span>
    );
  };

  renderTextInputElement(lastElement) {

    if (this.props.disableTextInput) {
      return (
        <span className="form__field__tag--container">
          {lastElement && this.renderValue(lastElement, 0)}
          <input
            type="text"
            className="form__field__tag--input"
            id={this.props.fieldName}
            ref={this.props.tagType + 'Input'}
            onChange={this.updateInput}
            value={this.state.inputString}
          />
        </span>
      );
    }

    return (
      <span className="form__field__tag--container">
        {lastElement && this.renderValue(lastElement, 0)}
        <input
          type="text"
          className="form__field__tag--input"
          id={this.props.fieldName}
          onKeyDown={this.processTagInput}
r         onChange={this.updateInput}
          value={this.state.inputString}
        />
      </span>
    );
  }

  renderBylineInstructions() {
    if (this.props.tagType === TagTypes.contributor) {
      return (
        <span className="form__field__instructions">
          Press enter to add byline as text
        </span>
      );
    }
  }

  renderInputElements() {

    const valueLength = this.props.tagValue.length;
    const lastElement = !valueLength || valueLength === 0
      ? null
      : this.props.tagValue[valueLength - 1];

    return (
      <div className="form__field__tag--selector">
        {valueLength
          ? this.props.tagValue.map((value, i) => {
              if (i < valueLength - 1) {
                return this.renderValue(value, i);
              }
            })
          : ''}

        {this.renderTextInputElement(lastElement)}

      </div>

    );

  }

  render() {
    return (
      <div className="form__row"
        onBlur={this.props.hideTagResults}
        onMouseDown={this.props.showTagResults}
      >

        <div className="form__label__layout">
          <label className="form__label">{this.props.fieldName}</label>
          {this.renderBylineInstructions()}
        </div>


        {this.renderInputElements()}

        <CapiSearch
          capiTags={this.props.capiTags}
          showTags={this.props.showTags}
          tagsToVisible={this.props.tagsToVisible}
          selectNewTag={this.selectNewTag}
          tagValue={this.props.tagValue}
          removeDupes={removeStringTagDuplicates}
        />

        <TagFieldValue tagValue={this.props.tagValue}/>
      </div>
    );
  }
};
