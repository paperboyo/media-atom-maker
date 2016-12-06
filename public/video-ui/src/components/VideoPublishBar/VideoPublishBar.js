import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';

export default class VideoPublishBar extends React.Component {

  videoHasUnpublishedChanges() {
    const changeDetails = this.props.video.contentChangeDetails
    const lastModified = changeDetails.lastModified && changeDetails.lastModified.date
    const published = changeDetails.published && changeDetails.published.date

    if (!published || lastModified > published) {
      return true
    }

    return false
  }

  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing === saveStateVals.inprogress;
  }

  render() {

    if (!this.props.video || !this.props.video.contentChangeDetails) {
        return false;
    }

    if (!this.videoHasUnpublishedChanges()) {
        return false;
    }

    if (this.videoIsCurrentlyPublishing()) {
      return (
        <div className="publish-bar">
          <span className="publish-bar__message">Publishing...</span>
        </div>
      );
    }

    return (
      <div className="publish-bar">
        <span className="publish-bar__message">This video atom has unpublished changes</span>
        <button type="button" className="publish-bar__button" onClick={this.props.publishVideo}>Publish Changes Now</button>
      </div>
    )
  }
}
