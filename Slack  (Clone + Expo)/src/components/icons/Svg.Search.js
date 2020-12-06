import * as React from 'react';
import PropTypes from 'prop-types';
import Svg, { Path } from 'react-native-svg';
import { colors } from '../../constants';

const SvgSearch = ({ size }) => (
  <Svg height={size} width={size} viewBox="0 0 32 32">
    <Path
      d="M21.388 21.141c-.045.035-.089.073-.132.116s-.08.085-.116.132c-.836.805-1.82 1.455-2.907 1.905-1.096.453-2.3.705-3.567.705s-2.471-.252-3.569-.707c-1.141-.472-2.169-1.165-3.031-2.028s-1.555-1.889-2.028-3.031c-.453-1.097-.705-2.301-.705-3.568s.252-2.471.707-3.569c.472-1.14 1.165-2.169 2.027-3.031s1.891-1.555 3.031-2.027c1.099-.455 2.303-.707 3.569-.707s2.471.252 3.569.707c1.141.472 2.169 1.165 3.031 2.028s1.555 1.889 2.028 3.031c.453 1.097.705 2.301.705 3.568s-.252 2.471-.707 3.569c-.451 1.087-1.1 2.071-1.905 2.907zm7.555 5.916l-4.9-4.9c.7-.875 1.28-1.849 1.715-2.901.587-1.416.909-2.967.909-4.589s-.323-3.173-.909-4.589c-.608-1.469-1.5-2.791-2.605-3.896s-2.427-1.997-3.896-2.605c-1.416-.587-2.967-.909-4.589-.909s-3.173.323-4.589.909c-1.469.608-2.791 1.5-3.896 2.605s-1.997 2.427-2.605 3.896c-.587 1.416-.909 2.967-.909 4.589s.323 3.173.909 4.589c.608 1.469 1.5 2.791 2.605 3.896s2.427 1.997 3.896 2.605c1.416.587 2.967.909 4.589.909s3.173-.323 4.589-.909c1.051-.436 2.027-1.016 2.901-1.715l4.9 4.9c.521.521 1.365.521 1.885 0s.521-1.365 0-1.885z"
      fill={colors.grey}
    />
  </Svg>
);

SvgSearch.defaultProps = {
  size: 24
};

SvgSearch.propTypes = {
  // optional
  size: PropTypes.number
};

export default React.memo(SvgSearch);
