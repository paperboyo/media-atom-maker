import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import SelectBox from '../FormFields/SelectBox';
import { fieldLengths } from '../../constants/videoEditValidation';
import { videoCategories } from '../../constants/videoCategories';
import { privacyStates } from '../../constants/privacyStates';
import ImageSelector from '../FormFields/ImageSelector';

class VideoEdit extends React.Component {
  hasCategories = () => this.props.youtube.categories.length !== 0;
  hasChannels = () => this.props.youtube.channels.length !== 0;

  componentWillMount() {
    if (!this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
  }

  render() {
    return (
      <div className="form__group">
        <ManagedForm
          data={this.props.video}
          updateData={this.props.updateVideo}
          editable={this.props.editable}
          updateErrors={this.props.updateErrors}
          formName={this.props.formName}
        >
          <ManagedField
            fieldLocation="title"
            name="Headline (YouTube title)"
            maxLength={fieldLengths.title}
            isRequired={true}
          >
            <TextInput />
          </ManagedField>
          <ManagedField
            fieldLocation="category"
            name="Category"
            isRequired={true}
          >
            <SelectBox selectValues={videoCategories} />
          </ManagedField>
          <ManagedField
            fieldLocation="posterImage"
            name="Poster Image"
            isRequired={true}
          >
            <ImageSelector />
          </ManagedField>
          <ManagedField
            fieldLocation="youtubeCategoryId"
            name="YouTube Category"
            isRequired={true}
          >
            <SelectBox selectValues={this.props.youtube.categories} />
          </ManagedField>
          <ManagedField
            fieldLocation="channelId"
            name="YouTube Channel"
            isRequired={true}
          >
            <SelectBox selectValues={this.props.youtube.channels} />
          </ManagedField>
          <ManagedField
            fieldLocation="privacyStatus"
            name="Privacy Status"
            isRequired={true}
          >
            <SelectBox selectValues={privacyStates} />
          </ManagedField>
        </ManagedForm>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCategories from '../../actions/YoutubeActions/getCategories';
import * as getChannels from '../../actions/YoutubeActions/getChannels';

function mapStateToProps(state) {
  return {
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch) {
  return {
    youtubeActions: bindActionCreators(
      Object.assign({}, getCategories, getChannels),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoEdit);
