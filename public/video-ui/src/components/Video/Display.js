import React from 'react';
import { Link } from 'react-router';
import VideoSelectBar from '../VideoSelectBar/VideoSelectBar';
import VideoPreview from '../VideoPreview/VideoPreview';
import VideoUsages from '../VideoUsages/VideoUsages';
import VideoData from '../VideoData/VideoData';
import Workflow from '../Workflow/Workflow';
import Icon from '../Icon';
import { formNames } from '../../constants/formNames';
import FieldNotification from '../../constants/FieldNotification';
import ReactTooltip from 'react-tooltip';
import { getStore } from '../../util/storeAccessor';
import { blankVideoData } from '../../constants/blankVideoData';

class VideoDisplay extends React.Component {
  componentWillMount() {
    this.props.videoActions.getUsages(this.props.params.id);

    if (this.props.route.mode === 'create') {
      this.props.videoActions.updateVideo(blankVideoData);
      this.props.videoActions.updateVideoEditState(true);
    } else {
      this.props.videoActions.getVideo(this.props.params.id);
    }
  }

  saveAndUpdateVideo = video => {
    if (this.props.route.mode === 'create') {
      this.props.videoActions.createVideo(video);
    } else {
      this.props.videoActions.saveVideo(video);
    }
  };

  updateVideo = video => {
    this.props.videoActions.updateVideo(video);
  };

  selectVideo = () => {
    window.parent.postMessage({ atomId: this.props.video.id }, '*');
  };

  manageEditingState = () => {
    if (this.props.videoEditOpen) {
      this.saveAndUpdateVideo(this.props.video);
    }

    this.props.videoActions.updateVideoEditState(!this.props.videoEditOpen);
  };

  cannotEditStatus = () => {
    return this.props.video.expiryDate <= Date.now();
  };

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  cannotCloseEditForm = () => {
    const formName = formNames.videoData;

    const errors = this.props.checkedFormFields[formName]
      ? this.props.checkedFormFields[formName]
      : {};
    return Object.keys(errors).some(field => {
      const value = errors[field];
      return value !== null;
    });
  };

  validateKeywords = keywords => {
    if (!Array.isArray(keywords) ||
        keywords.length === 0 ||
        keywords.every(keyword => {
          return keyword.match(/^tone/);
        })
       ) {
        return new FieldNotification(
          'desired',
          'A series or a keyword tag is required for creating composer pages',
          FieldNotification.warning
        );
    }
    return null;
  };

  handleAssetClick = e => {
    if (this.props.videoEditOpen) {
      e.preventDefault();
    }
  };

  renderEditButton = () => {
    if (this.props && this.props.videoEditOpen) {
      return (
        <button
          disabled={this.cannotCloseEditForm()}
          onClick={() => this.manageEditingState()}
        >
          <Icon
            className={
              'icon__done ' + (this.cannotCloseEditForm() ? 'disabled' : '')
            }
            icon="done"
          />
        </button>
      );
    } else {
      return (
        <button
          disabled={this.props.videoEditOpen}
          onClick={() => this.manageEditingState()}
        >
          <Icon
            className={
              'icon__edit ' + (this.props.videoEditOpen ? 'disabled' : '')
            }
            icon="edit"
          />
        </button>
      );
    }
  };

  renderPreview = () => {
    return (
      <div className="video__detailbox">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">Video Preview</header>
          <Link
            className={'button ' + (this.props.videoEditOpen ? 'disabled' : '')}
            to={`/videos/${this.props.video.id}/upload`}
            onClick={e => this.handleAssetClick(e)}
            data-tip="Edit Assets"
          >
            <Icon className="icon__edit" icon="edit" />
            <ReactTooltip place="bottom" />
          </Link>
        </div>
        <VideoPreview
          video={this.props.video || {}}
          saveAndUpdateVideo={this.saveAndUpdateVideo}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          config={this.props.config}
          videoEditOpen={this.props.videoEditOpen}
        />
      </div>
    );
  };

  render() {
    const video = this.props.video &&
      this.props.params.id === this.props.video.id
      ? this.props.video
      : undefined;

    if (!video) {
      return <div className="container">Loading... </div>;
    }

    return (
      <div>
        <VideoSelectBar
          video={video}
          onSelectVideo={this.selectVideo}
          publishedVideo={this.props.publishedVideo}
          embeddedMode={this.props.config.embeddedMode}
        />

        <div className="video">
          <div className="video__main">
            <div className="video__row">
              <div className="video__detailbox video__data">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">
                    Video Data
                  </header>
                  {this.renderEditButton()}
                </div>
                <VideoData
                  video={this.props.video || {}}
                  updateVideo={this.updateVideo}
                  editable={this.props.videoEditOpen}
                  formName={formNames.videoData}
                  updateErrors={this.props.formErrorActions.updateFormErrors}
                  updateWarnings={this.props.formErrorActions.updateFormWarnings}
                  validateKeywords={this.validateKeywords}
                />
              </div>
              {this.renderPreview()}
            </div>
            <div className="video__row">
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Usages</header>
                </div>
                <VideoUsages
                  video={this.props.video || {}}
                  publishedVideo={this.props.publishedVideo || {}}
                  usages={this.props.usages || []}
                />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Workflow</header>
                </div>
                <Workflow video={this.props.video || {}} />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as saveVideo from '../../actions/VideoActions/saveVideo';
import * as createVideo from '../../actions/VideoActions/createVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';
import * as videoUsages from '../../actions/VideoActions/videoUsages';
import * as getPublishedVideo
  from '../../actions/VideoActions/getPublishedVideo';
import * as updateVideoEditState
  from '../../actions/VideoActions/updateVideoEditState';
import * as updateFormErrors
  from '../../actions/FormErrorActions/updateFormErrors';
import * as updateFormWarnings
  from '../../actions/FormErrorActions/updateFormWarnings';

function mapStateToProps(state) {
  return {
    video: state.video,
    config: state.config,
    usages: state.usage,
    composerPageWithUsage: state.pageCreate,
    publishedVideo: state.publishedVideo,
    videoEditOpen: state.videoEditOpen,
    checkedFormFields: state.checkedFormFields,
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(
      Object.assign(
        {},
        getVideo,
        saveVideo,
        createVideo,
        updateVideo,
        videoUsages,
        getPublishedVideo,
        updateVideoEditState
      ),
      dispatch
    ),
    formErrorActions: bindActionCreators(
      Object.assign({}, updateFormErrors, updateFormWarnings),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
