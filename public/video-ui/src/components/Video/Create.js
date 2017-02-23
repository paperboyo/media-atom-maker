import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import SaveButton from '../utils/SaveButton';
import {blankVideoData} from '../../constants/blankVideoData';

class VideoCreate extends React.Component {

  componentDidMount() {
    this.props.videoActions.updateVideo(blankVideoData);
  }

  createVideo = () => {
    this.props.videoActions.createVideo(this.props.video);
  };

  resetVideo = () => {
    this.props.videoActions.updateVideo(blankVideoData);
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  render () {
    return (
      <div className="container">
        <form className="form create-form">
          <h1>Create new video</h1>
          <VideoEdit
            video={this.props.video}
            updateVideo={this.updateVideo}
            saveAndUpdateVideo={this.updateVideo}
            createMode
            editable
            saveState={this.props.saveState}
          />
          <SaveButton video={this.props.video} saveState={this.props.saveState} onSaveClick={this.createVideo} onResetClick={this.resetVideo} />
        </form>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createVideo from '../../actions/VideoActions/createVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';

function mapStateToProps(state) {
  return {
    video: state.video,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, updateVideo, createVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoCreate);
