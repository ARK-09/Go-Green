const isValidObject = (obj) => typeof obj === "object" && obj !== null;

const extractValidProperties = (input, propertiesToExtract) => {
  if (!Array.isArray(propertiesToExtract) || propertiesToExtract.length === 0) {
    throw new Error(
      "Invalid propertiesToExtract. It must be a non-empty array."
    );
  }

  if (Array.isArray(input)) {
    const resultArray = input
      .filter(isValidObject)
      .map((obj) => extractValidPropsFromObject(obj, propertiesToExtract));
    return resultArray;
  } else if (isValidObject(input)) {
    return extractValidPropsFromObject(input, propertiesToExtract);
  } else {
    return null;
  }
};

function extractValidPropsFromObject(object, validProperties) {
  const validObject = {};
  for (const property of validProperties) {
    if (
      object.hasOwnProperty(property) &&
      object[property] !== undefined &&
      object[property] !== null
    ) {
      validObject[property] = object[property];
    }
  }
  return validObject;
}

module.exports = extractValidProperties;
