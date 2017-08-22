import React from 'react';
import { Link } from 'react-router';
import VideoSearch from './VideoSearch/VideoSearch';
import VideoPublishBar from './VideoPublishBar/VideoPublishBar';
import AdvancedActions from './Videos/AdvancedActions';
import ComposerPageCreate from './Videos/ComposerPageCreate';
import Icon from './Icon';
import { Presence } from './Presence';

export default class Header extends React.Component {
  state = { presence: null };

  publishVideo = () => {
    this.props.publishVideo(this.props.video.id);
  };

  requiredComposerFieldsMissing = () => {
    return Object.keys(this.props.formFieldsWarning).some(key => {
      return this.props.formFieldsWarning[key];
    });
  };

  canonicalVideoPageExists = () => {
    return this.props.usages.totalVideoPages > 0;
  };

  renderProgress() {
    if (this.props.s3Upload.total) {
      // Start prompting the user about reloading the page
      window.onbeforeunload = () => {
        return false;
      };

      return (
        <progress
          className="topbar__progress"
          max={this.props.s3Upload.total}
          value={this.props.s3Upload.progress}
        />
      );
    } else {
      // Stop prompting the user. The upload continues server-side
      window.onbeforeunload = undefined;
      return false;
    }
  }

  renderHome() {
    return (
      <div className="flex-container topbar__global">
        <Link to="/" className="topbar__home-link" title="Home" />
      </div>
    );
  }

  renderSearch() {
    return (
      <div className="flex-container topbar__global">
        <VideoSearch {...this.props} />
      </div>
    );
  }

  renderHeaderBack() {
    return (
      <div className="flex-container topbar__global">
        <Link
          to={`/videos/${this.props.video.id}`}
          className="button"
          title="Back"
        >
          <Icon className="icon icon__back" icon="keyboard_arrow_left" />
        </Link>
        <span className="header__video__title">{this.props.video.title}</span>
      </div>
    );
  }

  renderHelpLink() {
    return (
      <nav className="topbar__nav-link">
        <Link className="button__secondary" to="/help">
          <Icon icon="live_help">Help</Icon>
        </Link>
      </nav>
    );
  }

  renderAuditLink() {
    const auditLink = '/videos/' + this.props.video.id + '/audit';
    return (
      <nav className="topbar__nav-link topbar__functional">
        <Link
          activeClassName="topbar__nav-link--active"
          className="button__secondary"
          to={auditLink}
        >
          View audit trail
        </Link>
      </nav>
    );
  }

  renderCreateVideo() {
    return (
      <nav className="topbar__nav-link">
        <Link className="btn" to="/videos/create">
          <Icon icon="add">Create</Icon>
        </Link>
      </nav>
    );
  }

  renderPresence() {
    // No indicator in the UI yet, just reporting back for use in Workflow
    if (this.props.presenceConfig) {
      return (
        <Presence video={this.props.video} config={this.props.presenceConfig} />
      );
    }

    return false;
  }

  renderComposerMissingWarning() {
    if (!this.requiredComposerFieldsMissing()) {
      return null;
    }

    if (this.canonicalVideoPageExists()) {
      return (
        <div className="header__fields__missing__warning">
          Fill in required composer fields before publishing
        </div>
      );
    }

    return (
      <div className="header__fields__missing__warning">
        Fill in required composer fields before creating video page
      </div>
    );
  }

  render() {
    const className = this.props.isTrainingMode
      ? 'topbar topbar--training-mode flex-container'
      : 'topbar flex-container';

    if (this.props.currentPath.endsWith('/upload')) {
      return (
        <header className={className}>
          {this.renderPresence()}
          {this.renderProgress()}
          {this.renderHeaderBack()}
        </header>
      );
    }
    if (!this.props.showPublishedState) {
      return (
        <header className={className}>
          {this.renderPresence()}
          {this.renderProgress()}

          {this.renderHome()}
          {this.renderSearch()}

          <div className="flex-spacer" />

          <div className="flex-container">
            {this.renderCreateVideo()}
            {this.renderHelpLink()}
          </div>

        </header>
      );
    } else {
      return (
        <header className={className}>
          {this.renderPresence()}
          {this.renderProgress()}
          {this.renderHome()}

          <VideoPublishBar
            className="flex-grow"
            video={this.props.video}
            publishedVideo={this.props.publishedVideo}
            editableFields={this.props.editableFields}
            saveState={this.props.saveState}
            videoEditOpen={this.props.videoEditOpen}
            updateVideoPage={this.props.updateVideoPage}
            usages={this.props.usages}
            publishVideo={this.publishVideo}
            formFieldsWarning={this.props.formFieldsWarning}
            requiredComposerFieldsMissing={this.requiredComposerFieldsMissing}
            canonicalVideoPageExists={this.canonicalVideoPageExists}
          />
          <AdvancedActions
            video={this.props.video || {}}
            usages={this.props.usages}
          />
          <ComposerPageCreate
            videoEditOpen={this.props.videoEditOpen}
            video={this.props.video || {}}
            createVideoPage={this.props.createVideoPage}
            requiredComposerFieldsMissing={this.requiredComposerFieldsMissing}
            canonicalVideoPageExists={this.canonicalVideoPageExists}
          />
          {this.renderComposerMissingWarning()}
          <div className="flex-container">
            {this.renderAuditLink()}
            {this.renderHelpLink()}
          </div>

        </header>
      );
    }
  }
}
