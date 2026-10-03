import { useMemo } from "react";
import DOMPurify from "dompurify";

// Job descriptions from job boards arrive as HTML. It is cleaned first, so
// scripts and other unsafe markup can never run, then shown as formatted text.
const ALLOWED_TAGS = [
  "p", "br", "div", "span", "ul", "ol", "li",
  "h1", "h2", "h3", "h4", "h5", "h6",
  "strong", "b", "em", "i", "u", "a", "hr",
];

function JobDescription({ html }) {
  const clean = useMemo(() => {
    const purifier = DOMPurify();

    // Links open in a new tab and cannot access the opener window.
    purifier.addHook("afterSanitizeAttributes", (node) => {
      if (node.tagName === "A") {
        node.setAttribute("target", "_blank");
        node.setAttribute("rel", "noopener noreferrer");
      }
    });

    return purifier.sanitize(html || "", {
      ALLOWED_TAGS,
      ALLOWED_ATTR: ["href", "target", "rel"],
    });
  }, [html]);

  if (!clean.trim()) {
    return <p>No job description available.</p>;
  }

  return (
    <div
      className="job-description-html"
      dangerouslySetInnerHTML={{ __html: clean }}
    />
  );
}

export default JobDescription;